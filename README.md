# uploader-s3jar-to-function


package package com.ituple.ci.s3trigger.callback; contains CallBack.java which is the class, will fetch all the information from S3 for uploaded file.

please find the below steps to trigger lambda to fetch all the information from S3 for uploaded file.


Step1: create jar for the project uploader-s3jar-to-function. mvn clean compile install (goto target folder and pick the jar file for this project)

Step2: create a lambda function

Step3: add trigger to the lambda (choose s3 to trigger this lamnda to get executed)
https://github.com/PiyushMittl/uploader-s3jar-to-function/blob/master/addtrigger.PNG

Step4: choose bucket and event type (Object created) and save the settings.

Step5: 
