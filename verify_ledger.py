import urllib.request
import urllib.error
import json
import sys

BASE_URL = "http://localhost:8080/api"

def make_request(method, url, data=None):
    req = urllib.request.Request(url, method=method)
    req.add_header('Content-Type', 'application/json')
    
    if data:
        json_data = json.dumps(data).encode('utf-8')
        req.data = json_data

    try:
        with urllib.request.urlopen(req) as response:
            if response.status not in (200, 201):
                print(f"Error: Status {response.status}")
                return None
            return json.loads(response.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        print(f"HTTP Error: {e.code} - {e.read().decode('utf-8')}")
        raise
    except Exception as e:
        print(f"Error: {e}")
        raise

def create_account(name, currency):
    print(f"Creating account for {name} ({currency})...")
    resp = make_request("POST", f"{BASE_URL}/accounts", {"name": name, "currency": currency})
    print(f"Created. ID: {resp}")
    return resp

def get_account(account_id):
    resp = make_request("GET", f"{BASE_URL}/accounts/{account_id}")
    return resp

def post_transaction(description, postings):
    print(f"Posting transaction: {description}")
    resp = make_request("POST", f"{BASE_URL}/transactions", {
        "description": description,
        "postings": postings
    })
    print(f"Transaction Posted. ID: {resp}")
    return resp

def main():
    try:
        # 1. Create Accounts
        alice_id = create_account("Alice", "USD")
        bob_id = create_account("Bob", "USD")

        # 2. Check Initial Balances
        alice = get_account(alice_id)
        bob = get_account(bob_id)
        print(f"Alice Initial Balance: {alice['balance']}")
        print(f"Bob Initial Balance: {bob['balance']}")

        # 3. Transaction: Alice pays Bob 100
        # Credit Alice (Decrease - assuming Asset), Debit Bob (Increase - assuming Asset)
        # Note: Currency is now required in PostingCommand
        postings = [
            {"accountId": alice_id, "amount": 100, "currency": "USD", "type": "CREDIT"},
            {"accountId": bob_id, "amount": 100, "currency": "USD", "type": "DEBIT"}
        ]
        
        post_transaction("Alice pays Bob 100", postings)

        # 4. Check Final Balances
        alice = get_account(alice_id)
        bob = get_account(bob_id)
        print(f"Alice Final Balance: {alice['balance']}")
        print(f"Bob Final Balance: {bob['balance']}")
        
        # New balance checks.
        # Credit to Asset Account -> Decrease. 0 - 100 = -100.
        # Debit to Asset Account -> Increase. 0 + 100 = 100.
        alice_bal = float(alice['balance'])
        bob_bal = float(bob['balance'])
        
        if alice_bal == -100.0 and bob_bal == 100.0:
            print("VERIFICATION SUCCESSFUL")
        else:
            print(f"VERIFICATION FAILED: Alice={alice_bal}, Bob={bob_bal}")
            sys.exit(1)

    except Exception as e:
        print(f"An error occurred: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
