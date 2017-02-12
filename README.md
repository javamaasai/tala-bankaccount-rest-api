# Tala Bank Account REST API

### Running

You need to download and install sbt for this application to run.

Once you have sbt installed, the following at the command prompt will start up Play in development mode:

```
sbt run
```

Play will start up on the HTTP port at http://localhost:9000/.   You don't need to reploy or reload anything -- changing any source code while the server is running will automatically recompile and hot-reload the application on the next HTTP request. 

### Usage

If you call the same URL from the command line, you’ll see JSON. Using httpie, we can execute the command:

[showjsondata]-> This basially shows the initial dataset used. The application uses map which are polulated for the duration of the application lifecycle.

```
http --verbose http://localhost:9000/showjsondata

{
	"bankAccounts" : [{
			"id" : 0,
			"accountName" : "Equity",
			"accountType" : "branch",
			"balance" : 3200
		}, {
			"id" : 1,
			"accountName" : "Mama mboga",
			"accountType" : "current",
			"balance" : 3000
		}, {
			"id" : 2,
			"accountName" : "Isaac khaguli",
			"accountType" : "current",
			"balance" : 200
		}
	],
	"transactionstypes" : [{
			"id" : 1,
			"name" : "Deposit",
			"dayLimit" : 150,
			"transactionLimit" : 40,
			"frequency" : 4
		}, {
			"id" : 2,
			"name" : "Withdrawal",
			"dayLimit" : 50,
			"transactionLimit" : 20,
			"frequency" : 3
		}
	],
	"transactions" : [{
			"id" : 1,
			"accountId" : 1,
			"transactionTypeid" : 1,
			"amount" : 300,
			"TansactionDate" : "10-2-2017"
		}, {
			"id" : 2,
			"accountId" : 2,
			"transactionTypeid" : 1,
			"amount" : 30,
			"TansactionDate" : "10-2-2017"
		}
	],
	"AccountDailyLimit" : [{
			"id" : "10-2-2017-1",
			"accountId" : 1,
			"depositTransactionTotal" : 100,
			"depositFrequencyTotal" : 2,
			"withdrawalTansactionTotal" : 20,
			"withdrawalFrequency" : 2,
			"dailyDate" : "10-2-2017"
		}, {
			"id" : "10-2-2017-2",
			"accountId" : 2,
			"depositTransactionTotal" : 30,
			"depositFrequencyTotal" : 3,
			"withdrawalTansactionTotal" : 40,
			"withdrawalFrequency" : 1,
			"dailyDate" : "10-2-2017"
		}
	]
}
```

[balance]-> This basially shows the outstanding balance in a users account

```
http --verbose http://localhost:9000/balance/XXXX
where XXXX is the account number e.g http://localhost:9000/balance/1 where 1 is account number for "mama mboga" i above json data

sample result: #> AccountName:Mama mboga #> Balance:3000
```

[deposit]-> deposits amount into users account.

```
http --verbose http://localhost:9000/deposit/XXXX/YYYY

Deposit endpoint credits the account with the specified amount
■ Max deposit for the day = $150K
■ Max deposit per transaction = $40K
■ Max deposit frequency = 4 transactions/day
```

[withdraw]-> withdraws amount from a users account

```
http --verbose http://localhost:9000/withdraw/XXXX/YYYY

Withdrawal endpoint deducts the account with the specified amount
■ Max withdrawal for the day = $50K
■ Max withdrawal per transaction = $20K
■ Max withdrawal frequency = 3 transactions/day
■ Cannot withdraw when balance is less than withdrawal amount
```

### Load Testing

The best way to see what Play can do is to run a load test.  We've included Gatling in this test project for integrated load testing.

Start Play in production mode, by [staging the application](https://www.playframework.com/documentation/2.5.x/Deploying) and running the play script:s

```
sbt stage
cd target/universal/stage
bin/play-rest-api -Dplay.crypto.secret=testing
```

Then you'll start the Gatling load test up (it's already integrated into the project):

```
sbt gatling:test
```

For best results, start the gatling load test up on another machine so you do not have contending resources.  You can edit the [Gatling simulation](http://gatling.io/docs/2.2.2/general/simulation_structure.html#simulation-structure), and change the numbers as appropriate.

Once the test completes, you'll see an HTML file containing the load test chart:

```
 ./rest-api/target/gatling/gatlingspec-1472579540405/index.html
```

That will contain your load test results.
