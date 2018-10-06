# webshoppe

A webshop project from 2015 rebooted.

![alt text](https://raw.githubusercontent.com/codingchili/webshoppe/master/scrapbook/2-styling-fixes.PNG "Current snapshot version")

### Background

The project is written as a java EE webshop application with the following stack
* MySQL
* Bootstrap
* HTML5
* JSP/JSTL

While we could have replaced these with something never and more interesting, I think
it would be more fun/challenging to keep the stack in place. I like to have some diversity
in my projects, not everything has to be NoSQL and SPA :) I'm not much for EE, application
servers, servlets and all that enterprisey stuff. 



Challenges
- performance
  - [ ] make sure to upgrade to latest MySQL DB / driver.
  - [ ] analyze existing queries, check for missing/bad indexes.
  - [ ] find the fastest goddamn application server there is.
  - [ ] server side rendering causes database calls to block.
    - even worse, all our DB calls are synchronous and serialized.
- security 
  - [ ] tons of forms here, we need some solid CSRF protection.
  - [ ] zero protection against XSS in place.
  - [ ] payment security; not required for simple swish integrations.
  - [ ] password hashing: uses PBKDF2, barely passable, upgrade to Argon2?
- mobile support
  - [ ] we use bootstrap so it shouldn't be too hard.
  - [ ] upgrade bootstrap from v3 to v4.
  - [ ] add a favicon / pwa manifest.
- containerless deployment
  - [ ] tom EE / undertow / ? (i will NEVER touch spring.)
- payment
  - [ ] there is no existing payment implementation.
  - [ ] lets start with swish, and just use a URI / QR for payments.
  - [ ] maybe later we can explore more options, Ether etc?

### Building
Super easy, 

```
./gradlew jar
```

Produces a standalone jar with an embedded application server.

### Installing

Needs at least one MySQL server, we are using 8.0.12 for development.

Preload a new database with the file `database.sql`.

The default configuration looks like,
```
{
  "jdbcUrl" : "jdbc:mysql://192.168.10.129:3306/webshop?useSSL=false",
  "databaseUser" : "root",
  "databasePass" : "root",
  "swishReceiver" : "07372151522"
}
``` 
This is my development settings, you you will need to place a file called `application.json` beside your
jar, with values that matches your environment.

## Contributing
Contributions are always welcome! pull requests, code reviews, new issues, comments on existing issues etc.