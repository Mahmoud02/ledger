# Ledger Service

A robust, heavy-lifting accounting ledger service built with **Spring Boot**, **Domain Driven Design (DDD)**, and **Hexagonal Architecture (Ports and Adapters)**. It handles double-entry bookkeeping, concurrent transaction processing, and automated fee deduction.

## 🚀 Features

*   **Domain Driven Design**: Rich domain model (`Transaction`, `Account`, `Posting`) encapsulating complex business rules, separated from persistence and delivery mechanisms.
*   **Double-Entry Bookkeeping**: Ensures every transaction is balanced (Debits = Credits).
*   **Hexagonal Architecture**: Core domain logic is isolated from external frameworks and databases.
*   **Concurrency Control**: Uses **Pessimistic Locking** (`SELECT ... FOR UPDATE`) to prevent race conditions during concurrent transfers and balance updates.
*   **Automated Transfer Fees**: 10% fee is automatically deducted from `E2E Transfers` and credited to the **Company Wallet**.
*   **System Accounts Bootstrapping**: Automatically initializes "Genesis" (Equity) and "Company Revenue" (Asset) accounts on startup.
*   **API Security**: Public API only allows creation of User Wallets (Assets), preventing manipulation of system accounts.

## 🏗 Architecture

The project follows strict Hexagonal Architecture principals:

*   **Domain**: `com.mahmoud.ledger.domain` - Pure Java business logic (Entities, Value Objects).
*   **Application**: `com.mahmoud.ledger.application` - Use Cases, Ports (Input/Output interfaces), and Services.
*   **Infrastructure**: `com.mahmoud.ledger.infrastructure` - Adapters for Persistence (JPA/H2) and Web Layer (REST Controllers).

## 🛠 System Accounts

The system automatically bootstraps the following immutable accounts:

| Account Name | Type | Interaction |
| :--- | :--- | :--- |
| **Genesis** | `EQUITY` | The source of all money entering the system (Deposits). |
| **Company Revenue** | `ASSET` | Receives the 10% fee from user-to-user transfers. |

## 🔌 API Reference

### 1. Create a User Wallet
Creates a new user account. Default type is `ASSET`.

**POST** `/api/accounts`
```json
{
  "name": "Alice Wallet",
  "currency": "USD"
}
```

### 2. Deposit Funds (Genesis -> User)
To introduce money into the system, create a transaction allowing "Genesis" to credit the user.

**POST** `/api/transactions`
```json
{
  "description": "Deposit via Stripe",
  "postings": [
    {
      "accountId": "00000000-0000-0000-0000-000000000001", // Genesis ID
      "amount": 100.00,
      "currency": "USD",
      "type": "CREDIT"
    },
    {
      "accountId": "<USER_UUID>",
      "amount": 100.00,
      "currency": "USD",
      "type": "DEBIT"
    }
  ]
}
```

### 3. Transfer Funds (User -> User) & Fee Deduction
Transfers funds between users. If `revenueAccountId` is provided, a 10% fee is automatically calculated and diverted to the Company Revenue account.

**POST** `/api/transfers`
```json
{
  "fromAccountId": "<ALICE_UUID>",
  "toAccountId": "<BOB_UUID>",
  "amount": 50.00,
  "currency": "USD",
  "description": "Payment for services",
  "revenueAccountId": "00000000-0000-0000-0000-000000000002" // Company Revenue ID
}
```

**Outcome:**
*   **Alice** is Debited 50.00
*   **Bob** is Credited 45.00
*   **Company Revenue** is Debited (Asset Increase) 5.00

### 4. Get Account Balance
**GET** `/api/accounts/{id}`

## 🧪 Testing

Run the full integration validation suite:
```bash
mvn test
```
Includes 25 comprehensive tests ensuring locking correctness, data integrity, and fee logic.

---

## 📚 Business Logic: The Genesis Problem

### Architecting a Double-Entry Ledger for Digital Wallets

#### Introduction
In the world of Fintech, the most fundamental challenge isn't moving money; it is tracking it correctly. When building a digital wallet or payment system (similar to PayPal or a Neo-bank), developers often encounter a logical paradox at the very beginning: **"Where does the first dollar come from?"**

If every transaction requires a source and a destination, how do you introduce funds into a system that starts with a zero balance? This section explores the business logic and architectural patterns behind solving this problem using Double-Entry Bookkeeping, Account Types, and the concept of the Genesis Account.

#### 1. The Core Dilemma: "You Can't Subtract from Zero"
In a naive implementation of a ledger, developers might apply a single rule for all accounts:
*   **Credit**: Subtract money (Spend).
*   **Debit**: Add money (Receive).

**The Problem**: When you attempt to "seed" the system—for example, giving a user named Alice her first $100—you must take that money from a system account (let's call it "Genesis").
*   **Transaction**: Credit Genesis $100 -> Debit Alice $100.
*   **Result**: The system checks Genesis, sees a balance of 0, attempts to subtract 100, and throws an "Insufficient Funds" error.

To solve this, we must introduce **Account Types**, which change the mathematical rules of the ledger.

#### 2. The Solution: ASSET vs. EQUITY
To build a compliant financial system, we must classify accounts into two distinct categories with opposing behaviors. This is based on the fundamental accounting equation: `Assets = Liabilities + Equity`.

**The Rules of Physics**

| Account Type | Real-World Analog | Debit Behavior | Credit Behavior |
| :--- | :--- | :--- | :--- |
| **ASSET** | User Wallets (Alice, Bob) | **Increase (+)** (Receiving Funds) | **Decrease (-)** (Spending Funds) |
| **EQUITY** | The System Source (Genesis) | **Decrease (-)** (Burning Funds) | **Increase (+)** (Minting Funds) |

**The "Genesis" Concept**
The Genesis Account is an **EQUITY** account. It does not represent a "vault" that holds money; rather, it represents the *source* or the net worth of the system.
When we Credit Genesis, we are not spending. We are increasing the system's capital. This allows us to "mint" money into existence without needing a prior balance.

#### 3. Financial Workflows (The Lifecycle of Money)

**A. Minting (Deposits / Cash-In)**
When Alice deposits $100 via her bank, the system must create a digital representation of that money.
*   **Action**: Credit Genesis $100, Debit Alice $100.
*   **Logic**: The system capital increases (Genesis +100), and Alice's asset increases (Alice +100). The equation remains balanced.

**B. Peer-to-Peer Transfer with Revenue (The Business Model)**
Alice sends $100 to Bob. The platform charges a $2.00 fee. This requires a Split Transaction.
*   **Action**:
    1.  Credit Alice $100.00 (Full amount leaves Alice).
    2.  Debit Bob $98.00 (Net amount enters Bob).
    3.  Credit Revenue Account $2.00 (Wait! Revenue behaves like Equity/Liability, but in our system we use an Asset Wallet for collection).
    *   *System Note*: In this codebase, the **Company Revenue** account is treated as an **ASSET** (Debit to Increase) aka "Company Wallet" to simplify collection.
    *   *Correction*: Therefore, we **Debit** the Revenue Account $2.00.

**C. Redemption (Withdrawal / Cash-Out)**
Alice withdraws $100 to her real bank account. We must "destroy" the digital value.
*   **Action**: Credit Alice $100, Debit Genesis $100.
*   **Logic**: Alice's balance returns to zero. The Genesis balance (system liability) reduces by 100. The loop is closed.

#### 4. The Real World: The "FBO" Model
A critical distinction must be made between the System Ledger (Database) and Real Money (Bank).

**How Companies like PayPal Work**
PayPal does not act as a central bank; they cannot print money. They operate on a 1:1 Backing Model.
1.  **Custody**: When users deposit money, PayPal moves the real funds into a pooled bank account known as an **FBO Account** (For Benefit Of).
2.  **Mirroring**: The digital balance in the user's app is merely a "mirror" or a claim against those funds sitting in the FBO account.

**The Golden Rule**: `Sum(User Wallets) <= Real Cash in Bank`
If the Genesis balance in the database exceeds the actual cash in the bank account, the company is insolvent.

#### 5. Architecture: The Layers of Payment
How does a Fintech connect the entire world? It uses a layered architecture:

*   **The Card Rails (Visa/Mastercard)**: Used for instant consumer reach. Acts as a fast highway for moving funds between user banks and the Fintech's FBO account.
*   **Correspondent Banking (SWIFT)**: Used for cross-border settlements. Since a US bank cannot talk directly to an Egyptian bank, they use intermediary "Correspondent Banks" (like JP Morgan) to bridge the gap.
*   **The Ledger Layer (The "Instant" Illusion)**: Real bank transfers take days (Settlement). Fintechs (like PayPal) provide instant transfers between users by simply updating their **internal database** (Asset/Equity logic) 
#### 6. Advanced Concepts: Event Sourcing
While this specific implementation relies on **State-Based Persistence** (storing the current balance in a database column for locking efficiency), high-scale ledger systems often utilize **Event Sourcing**.

**The Concept**:
Instead of storing "Current State" (e.g., `Alice Balance: $50`), you store only the **events** that led to that state.
*   `Event 1`: AccountOpened (Balance = 0)
*   `Event 2`: FundsDeposited (+$100)
*   `Event 3`: FundsTransferred (-$50)

**Deriving State**:
To determine the balance, the system "replays" all events from the beginning of time: `0 + 100 - 50 = $50`.

**Benefits**:
1.  **Time Travel**: "What was Alice's balance exactly 30 days ago?" -> Replay events up to that timestamp.
2.  **Perfect Audit**: The database *is* the audit log. History cannot be mutated, only appended to.
3.  **Resilience**: If the balance cache becomes corrupted, it can be rebuilt perfectly from the event log.

**Our Approach (Hybrid)**:
This project uses a hybrid, pragmatic approach common in many banking systems:
*   **State (Account Entity)**: We store the current balance (Snapshot) to allow for performant `SELECT FOR UPDATE` locking and instant reads.
*   **History (Transaction/Posting Entities)**: We effectively store the "Events" as the Transaction Log. The Balance is the "Materialized View" of this log.

#### Conclusion
Building a financial system requires a shift in mindset from simple arithmetic to **Accounting Logic**. By utilizing Account Types (Asset vs. Equity), we solve the "creation of money" problem elegantly.

The Genesis Account is not a debt; it is a record of the value the system has issued. By keeping the Revenue separate from Genesis, and strictly validating that digital assets match real-world bank balances, we ensure a robust, auditable, and profitable financial platform.


