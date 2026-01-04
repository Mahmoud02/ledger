# Ledger Service

A robust, heavy-lifting accounting ledger service built with **Spring Boot**, **Domain Driven Design (DDD)**, and **Hexagonal Architecture (Ports and Adapters)**. It handles double-entry bookkeeping, concurrent transaction processing, and automated fee deduction.

---

##  Business Logic

### 1. The Fundamental Challenge: Tracking Value
In a financial system, the most critical requirement is **Tracking Money Movement**.
A naive implementation might treat money like a mutable counter:
`UPDATE accounts SET balance = balance + 100 WHERE id = Alice`

**Why this fails:**
*   **No Audit Trail**: If Alice has $100 today and $50 tomorrow, you don't know *why*. Did she spend it? Was it stolen? Was it a bug?
*   **Data Integrity**: If the database crashes mid-update (`Credit Alice` succeeds, `Debit Bob` fails), money effectively disappears or is created out of thin air.

**The Solution: Double-Entry Ledger**
To solve this, we never "update" a balance directly. Instead, we record a **Transaction**—an immutable record of movement.
*   **Rule**: `Sum(Credits) == Sum(Debits)`. Every dollar moved MUST come from somewhere and go somewhere.
*   **Balance**: The balance is simply the derivative sum of all past transactions.

---

### 2. The Genesis Paradox: "Where does the first dollar come from?"
Once we enforce the Double-Entry rule ("Money must come from somewhere"), we hit a logical wall: **How do we introduce money into the system?**
If Alice deposits $100, we can't just "Credit Alice". We must "Debit" something. But if the system starts at zero, debiting anything makes it negative.

*   **The Dilemma**: We need a source of funds that *can* go negative without breaking physics.

### 3. The Solution: ASSET vs. EQUITY
To solve the Genesis Paradox, we use Accounting Physics. We classify accounts into two types with opposing behaviors:

| Account Type | Real-World Analog | Debit Behavior | Credit Behavior |
| :--- | :--- | :--- | :--- |
| **ASSET** | User Wallets (Alice, Bob) | **Increase (+)** (Receiving Funds) | **Decrease (-)** (Spending Funds) |
| **EQUITY** | The System Source (Genesis) | **Decrease (-)** (Burning Funds) | **Increase (+)** (Minting Funds) |

**The "Genesis" Account**
*   **Type**: `EQUITY`
*   **Role**: It represents the **System's Capital**. When we "Debit" Alice (Asset +), we "Credit" Genesis (Equity +). This records that the system has *issued* liability.

### 4. Financial Workflows
**A. Minting (Depositing Funds)**
`Genesis (Credit 100) -> Alice (Debit 100)`
*   Alice's Balance: +$100.
*   System Capital: +$100. (Balanced)

**B. Peer-to-Peer Transfer (Moving Funds)**
`Alice (Credit 50) -> Bob (Debit 50)`
*   Alice: -50.
*   Bob: +50.
*   System Capital: Unchanged.

**C. Fee Collection (The Business Model)**
For a $50 transfer with a 10% fee:
1.  `Alice (Credit 50)` -> Leaves Alice.
2.  `Bob (Debit 45)` -> Enters Bob.
3.  `Company Revenue (Debit 5)` -> Enters Company Wallet (Asset).

### 5. Advanced Concepts: Event Sourcing

**The Problem: Loss of History**
In traditional "State-Based" systems, databases overwrite data.
*   *Monday*: Alice has $100. (Database says: `Balance=100`)
*   *Tuesday*: Alice spends $50. (Database says: `Balance=50`)
We have lost the information that she *ever* had $100. If we suspect a bug caused the balance to drop, we cannot prove it. We have lost the narrative.

**The Solution: Store Events, Not State**
Event Sourcing flips the model. We do not store the "Current Balance". We store the *immutable facts* of what happened.

*   **Event 1**: `AccountCreated` (Balance 0)
*   **Event 2**: `FundsDeposited` (Amount: +100)
*   **Event 3**: `TransferSent` (Amount: -50)

To find the current balance, we simply replay the math: `0 + 100 - 50 = $50`.

**Implementation in this Project**
While this ledger uses a "Snapshot" approach (storing the current balance column) for performance and locking efficiency, it strictly adheres to Event Sourcing principles for data integrity:
*   **Immutability**: The `Transaction` table is never updated or deleted. It is an append-only log.
*   **Replayability**: If the `Account` table were corrupted or deleted, we could theoretically rebuild every user's balance to the exact penny by replaying the `Transaction` history.
*   **Audit**: The database *is* the audit log.

---

## Domain Driven Design (DDD)

Ideally, complex business software should be built around a rich model of the domain. DDD is the philosophy we used to ensure the software matches the mental model of financial experts.

### Why DDD?
Financial systems are complex. The rules for money movement, currency validation, and account types are not "technical" constraints—they are **Business Invariants**. DDD allows us to encapsulate these rules in the core, preventing invalid states (like creating money out of thin air or unbalanced transactions).

### 1. Strategic Design
Strategic design deals with large-scale architectural decisions and how different parts of the system interact.

#### Bounded Context
*   **The Concept**: A logical boundary within which a specific domain model applies. Words have specific meanings inside this boundary that might be different outside.
*   **In this Project**: The "Ledger" is its own Bounded Context. Inside here, terms like "Posting" or "Credit" have precise mathematical definitions. We do not care about "User Profiles" or "Marketing Emails"—those belong to other contexts.

#### Ubiquitous Language
*   **The Concept**: A common, rigorous language shared by developers and domain experts. Code should read like spoken language.
*   **In this Project**: We explicitly use terms like `DepositFunds`, `TransferFunds`, and `makeTransfer` instead of generic `update` or `save`.

### 2. Tactical Design
Tactical design focuses on the low-level building blocks within the domain model.

#### Aggregate Root
*   **The Concept**: A cluster of domain objects that can be treated as a single unit. The Root is the only component that can be loaded or referenced directly. It ensures consistency.
*   **In this Project**:
    *   `Transaction`: The transaction is the consistency boundary. You cannot have a `Posting` without a `Transaction`. The Transaction Aggregate ensures that `Sum(Debits) == Sum(Credits)` before the object is even valid.

#### Value Objects
*   **The Concept**: Objects that are defined by their attributes, not their identity. They are immutable. $5 is always $5; it doesn't matter *which* $5 bill it is.
*   **In this Project**:
    *   `Money`: We never use raw `BigDecimal`. We use a `Money` object that encapsulates amount and currency, preventing errors like adding USD to EUR.

#### Use Cases (Application Services)
*   **The Concept**: A layer that orchestrates domain objects to perform a business task.
*   **In this Project**: We expose intentional commands (`DepositFundsCommand`) rather than CRUD.

---

## Features

*   **Double-Entry Bookkeeping**: Ensures every transaction is balanced (Debits = Credits).
*   **Concurrency Control**: Uses **Pessimistic Locking** (`SELECT ... FOR UPDATE`) to prevent race conditions.
*   **Automated Transfer Fees**: 10% fee is automatically deducted from Transfers.
*   **System Accounts Bootstrapping**: Automatically initializes "Genesis" and "Revenue" accounts.
*   **Secure API patterns**: Hides internal system definitions (Genesis IDs) from the public API.

## Architecture

The project follows strict **Hexagonal Architecture** (Ports and Adapters):

*   **Domain**: `com.mahmoud.ledger.domain` - Pure Java business logic (Entities, Value Objects).
*   **Application**: `com.mahmoud.ledger.application` - Use Cases, Ports (Input/Output interfaces).
*   **Infrastructure**: `com.mahmoud.ledger.infrastructure` - Adapters for Persistence (JPA) and Web (Spring REST).

## System Accounts

The system automatically bootstraps the following immutable accounts:

| Account Name | Type | Interaction |
| :--- | :--- | :--- |
| **Genesis** | `EQUITY` | The source of all money entering the system (Deposits). |
| **Company Revenue** | `ASSET` | Receives fees from user transfers. |

## API Reference

### 1. Create a User Wallet
**POST** `/api/accounts`
```json
{ "name": "Alice Wallet", "currency": "USD" }
```

### 2. Deposit Funds (Genesis -> User)
**POST** `/api/deposits`
```json
{ "accountId": "<ALICE_UUID>", "amount": 100, "currency": "USD" }
```
*   *Secure*: The system automatically debits the hidden "Genesis" Equity account.

### 3. Transfer Funds (User -> User)
**POST** `/api/transfers`
```json
{ "fromAccountId": "<ALICE_UUID>", "toAccountId": "<BOB_UUID>", "amount": 50, "currency": "USD" }
```
*   *Fee Logic*: Automatically deducts 10% fee and routes it to the "Company Revenue" account.

## Testing

Run the full suite:
```bash
mvn test
```
includes 25+ tests covering Unit, Integration, and Concurrency scenarios.
