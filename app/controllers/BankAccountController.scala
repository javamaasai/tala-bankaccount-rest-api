/**
  * Copyright 2017 Isaac khaguli
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at

  *    http://www.apache.org/licenses/LICENSE-2.0

  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * 
  **/
  
package controllers

import play.api._
import play.api.libs.json._
import play.api.mvc._
import models.entities._
import scala.collection._
import scala.collection.convert.decorateAsScala._
import java.util.concurrent.ConcurrentHashMap
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Calendar
import java.lang.NullPointerException
  
class BankAccountController extends Controller{
    
    val DATE_PATTERN = "dd-MM-yyyy"
    
    //Using map is better for large data
    val bankAccountsMap = new ConcurrentHashMap[String, Object]()
    bankAccountsMap.put("0", Account(0, "Equity", "branch", 3200))
    bankAccountsMap.put("1", Account(1, "Mama mboga", "current", 3000))
    bankAccountsMap.put("2", Account(2, "Isaac khaguli", "current", 200))
    
    val transactionTypeMap = new ConcurrentHashMap[String, Object]()
    transactionTypeMap.put("1", TransactionType(1, "Deposit", 150, 40, 4))
    transactionTypeMap.put("2", TransactionType(2, "Withdrawal", 50, 20, 3))
    
    val transactionListMap = new ConcurrentHashMap[String, Object]()
    transactionListMap.put("1", Transactions(1, 1, 1, 300, "10-2-2017"))
    transactionListMap.put("2", Transactions(2, 2, 1, 30, "10-2-2017"))
    
    val AccountDailyLimitMap = new ConcurrentHashMap[String, Object]()
    AccountDailyLimitMap.put("10-2-2017-1", AccountDailyLimit("10-2-2017-1",1,100,2,20,2,"10-2-2017"))
    AccountDailyLimitMap.put("10-2-2017-2", AccountDailyLimit("10-2-2017-2",2,30,3,40,1,"10-2-2017"))
    
    /**
     * Function to convert int to string
     **/
    def toInt(s: String): Int = {
          try {
                s.toInt
          } catch {
                case e: Exception => 0
          }
    }
    
    /**
     * Function to convert get currect date string
     **/
    def getCurDate(p: String): String = {
        val today = Calendar.getInstance().getTime()
        // create the date/time formatters
        val selectFormat = new SimpleDateFormat(p)
        val selectDate = selectFormat.format(today)
        
        return selectDate
    }
    
    /**
     * Action Endpoint to show initial data in json format
     **/
    def showJsonData = Action { implicit request =>
        println("******* show JSON data ************")
        val bankAccounts = List (
            bankAccountsMap.get("0").asInstanceOf[Account],
            bankAccountsMap.get("1").asInstanceOf[Account],
            bankAccountsMap.get("2").asInstanceOf[Account]
        )
        
        implicit val BankAccountWrites = Json.writes[Account]
        val transactionTypesList = List (
            transactionTypeMap.get("1").asInstanceOf[TransactionType],
            transactionTypeMap.get("2").asInstanceOf[TransactionType]
        )
        
        implicit val transactionTypesWrites = Json.writes[TransactionType]
        val transactionsList = List(
            transactionListMap.get("1").asInstanceOf[Transactions],
            transactionListMap.get("2").asInstanceOf[Transactions]
        )
        
        implicit val transactionListWrites = Json.writes[Transactions]
        val AccountDailyLimitList = List(
            AccountDailyLimitMap.get("10-2-2017-1").asInstanceOf[AccountDailyLimit],
            AccountDailyLimitMap.get("10-2-2017-2").asInstanceOf[AccountDailyLimit]
        )
        
        implicit val AccountDailyLimitListWrites = Json.writes[AccountDailyLimit]
            
        val json1: JsValue = Json.obj(
            "bankAccounts" -> Json.toJson(bankAccounts),
            "transactionstypes" -> Json.toJson(transactionTypesList),
            "transactions" -> Json.toJson(transactionsList),
            "AccountDailyLimit" -> Json.toJson(AccountDailyLimitList)
        )
        Ok(json1)
    }
    
    /**
     * Action endpoint to get the balance
     **/
    def balance (accountnumber: String) = Action {
        println("******* check balance ************")
        var res_message = new String
        if (bankAccountsMap.containsKey(accountnumber)){
            val sel_account: Account = bankAccountsMap.get(accountnumber).asInstanceOf[Account]
            res_message = "#> AccountName:"+sel_account.accountName+" #> Balance:"+sel_account.balance
        }else{
             res_message = "Error! Account does not exist."
        }
        Ok(res_message)
    }
    
    /**
     * action endpoint to deposit amount
     **/
    def deposit (accountnumber: String, amount: String) = Action {
        println("******* deposit ************")
        var res_message = new String
        var sel_AccountDailyLimit: AccountDailyLimit = null
        if (bankAccountsMap.containsKey(accountnumber)){
            val sel_account: Account = bankAccountsMap.get(accountnumber).asInstanceOf[Account]
            val sel_transactionType = transactionTypeMap.get("1").asInstanceOf[TransactionType]
            
            //Test for transaction limit
            if(amount.toLong > sel_transactionType.transactionLimit){
                res_message = "Exceeded Maximum deposit per transaction"
            }else{
                val sel_accountLimitId: String = getCurDate(DATE_PATTERN)+"-"+sel_account.id
                if(AccountDailyLimitMap.containsKey(sel_accountLimitId)){
                    sel_AccountDailyLimit = AccountDailyLimitMap.get(sel_accountLimitId).asInstanceOf[AccountDailyLimit]
                } else{
                    sel_AccountDailyLimit = AccountDailyLimit(sel_accountLimitId,sel_account.id,0,0,0,0,getCurDate(DATE_PATTERN))
                    AccountDailyLimitMap.put(sel_accountLimitId, sel_AccountDailyLimit)
                }
                
                //Test for deposit frequency
                if(sel_AccountDailyLimit.depositFrequencyTotal < sel_transactionType.frequency){
                    val tran_limit = sel_transactionType.dayLimit
                    val cur_tran_limit = sel_AccountDailyLimit.depositTransactionTotal
                    val diff_limit = tran_limit - cur_tran_limit
                    
                    //Test for daily deposit limit
                    if(diff_limit >= amount.toLong){
                        AccountDailyLimitMap.replace(sel_accountLimitId, AccountDailyLimit(sel_accountLimitId,sel_account.id,
                        (sel_AccountDailyLimit.depositTransactionTotal + amount.toLong),(sel_AccountDailyLimit.depositFrequencyTotal + 1),
                        sel_AccountDailyLimit.withdrawalTansactionTotal,sel_AccountDailyLimit.withdrawalFrequency,sel_AccountDailyLimit.dailyDate))
                        bankAccountsMap.replace(accountnumber, Account(sel_account.id, sel_account.accountName, sel_account.accountType, 
                        (sel_account.balance + amount.toLong)))
                        
                        res_message = "deposit success"
                    }else{
                        res_message = "Exceeded Maximum deposit per day"
                    }
                }else{
                    res_message = "Exceeded Maximum deposit frequency per day"
                }
            }
        }else{
            res_message = "account does not exist!"
        }
        Ok(res_message)
    }
    
    /**
     * Action endpoint to withdraw
     **/
    def withdraw (accountnumber: String, amount: String) = Action {
        println("####### Withdraw ############")
        var res_message = new String
        var sel_AccountDailyLimit: AccountDailyLimit = null
        if (bankAccountsMap.containsKey(accountnumber)){
            val sel_account: Account = bankAccountsMap.get(accountnumber).asInstanceOf[Account]
            val sel_transactionType = transactionTypeMap.get("2").asInstanceOf[TransactionType]
            
            //Test withdraw per tramsaction limit
            if(amount.toLong > sel_transactionType.transactionLimit){
                res_message = "Exceeded Maximum withdrawal per transaction"
            }else{
                val sel_accountLimitId: String = getCurDate(DATE_PATTERN)+"-"+sel_account.id
                if(AccountDailyLimitMap.containsKey(sel_accountLimitId)){
                    sel_AccountDailyLimit = AccountDailyLimitMap.get(sel_accountLimitId).asInstanceOf[AccountDailyLimit]
                } else{
                    sel_AccountDailyLimit = AccountDailyLimit(sel_accountLimitId,sel_account.id,0,0,0,0,getCurDate(DATE_PATTERN))
                    AccountDailyLimitMap.put(sel_accountLimitId, sel_AccountDailyLimit)
                }
                
                //Test withdraw per day frequency
                if(sel_AccountDailyLimit.withdrawalFrequency < sel_transactionType.frequency){
                    val tran_limit = sel_transactionType.dayLimit
                    val cur_tran_limit = sel_AccountDailyLimit.withdrawalTansactionTotal
                    val diff_limit = tran_limit + cur_tran_limit
                    
                    //Test Withdraw per day limit
                    if(diff_limit > amount.toLong){
                        
                        //Test if amount over balance 
                        if(sel_account.balance > amount.toLong){
                            AccountDailyLimitMap.replace(sel_accountLimitId, AccountDailyLimit(sel_accountLimitId,sel_account.id,
                            (sel_AccountDailyLimit.withdrawalTansactionTotal - amount.toLong),(sel_AccountDailyLimit.withdrawalFrequency + 1),
                            sel_AccountDailyLimit.withdrawalTansactionTotal,sel_AccountDailyLimit.withdrawalFrequency,sel_AccountDailyLimit.dailyDate))
                            bankAccountsMap.replace(accountnumber, Account(sel_account.id, sel_account.accountName, sel_account.accountType, 
                            (sel_account.balance - amount.toLong)))
                            
                            res_message = "withdraw success"
                        }
                        else{
                            res_message = "Exceeded balance in account"
                        }
                    }else{
                        res_message = "Exceeded Maximum withdrawal per day"
                    }
                }else{
                    res_message = "Exceeded Maximum withdrawal frequency per day"
                }
            }
        }else{
            res_message = "account does not exist!"
        }
        Ok(res_message)
    }
    
    def index = Action { implicit request =>
        Ok(views.html.index())
    }
}