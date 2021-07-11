# techtest

One of the strangest assignment I have ever received for a tech test; It had 60+ files for a 1 hour submission, too many libraries, archaic application design, and strange expectations (testing generated code). If were to ever test a candidate using this as a template, I would rather pair with them to guide them and see how they approach the problem. 

 
#### Notes
* Exercise 1 : Test fixed and functionality added. Ideally few test should be deleted as they
are testing dto/lombok generated code. This is generally not needed as we can always write some scenario based
tests and also exclude them from coverage reports (if that was the primary motivation for adding them).

* Exercise 2 : The MD5_CHECKSUM defined in TechTestApplication is for *.DataBody.dataBody only, so I have also
 implemented the same, the header is not part of the hash calculations. 

* Exercise 4 : The Patch implementation is a bit unconventional due to the specified URL format. 

* Exercise 5 : For lake timeout, retry is currently done at the most 10 times. 
