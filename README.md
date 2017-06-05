QR Emergencias
====================

Installation
------------

- Prerequisites (see _Technology Stack_):

    * JDK 8
    * gradle >= 3.0

- Install:
    ```bash
    cd ~/code
    git clone git@github.com
    cd qremergencias
    git checkout develop
    ./gradlew compileJava
    ```

Run server
----------

```bash
./gradlew :[qremergencias-bo|qremergencias-ws]:bootRun
```

To initialize the database go to application.properties and set the value of spring.datasource.initialize to true. Revert this change for future runs.

- Server will run on [http://localhost:8080/](http://localhost:8080/)

Build
-----

```bash
./gradlew build -Denv=gl
```

# QR Emergencias WS API

Get Token
-----------------

    URL: /api/token
    METHOD: GET
    DESCRIPTION: Obtain CSRF token for any future POST, PUT, DELETE

Login
-----------------------

    URL: /api/login
    METHOD: POST

REQUEST HEADERS

| HEADER | VALUE |
| ------ | ----- |
| Content-Type  | application/x-www-form-urlencoded  |

REQUEST PARAMETERS

| PARAM  | TYPE | REQUIRED | EXAMPLE |
| ------ | ---- | -------- | ------- |
| username  | String | yes | user |
| password  | String | yes | Passw0rd! |
| _csrf     | String | yes | 0522f89b-d77c-483e-88ba-f065c486efce |

REQUEST EXAMPLE

POST /api/login HTTP/1.1
Content-Length: 76
Content-Type: application/x-www-form-urlencoded

username=user&password=Passw0rd!&_csrf=0522f89b-d77c-483e-88ba-f065c486efce

Logout
-----------------------

    URL: /api/logout
    METHOD: POST

REQUEST HEADERS

| HEADER | VALUE |
| ------ | ----- |
| Content-Type  | application/x-www-form-urlencoded  |

REQUEST PARAMETERS

| PARAM  | TYPE | REQUIRED | EXAMPLE |
| ------ | ---- | -------- | ------- |
| _csrf     | String | yes | 0522f89b-d77c-483e-88ba-f065c486efce |

REQUEST EXAMPLE

POST /api/logout HTTP/1.1
Content-Length: 43
Content-Type: application/x-www-form-urlencoded

_csrf=0522f89b-d77c-483e-88ba-f065c486efce

Register
-----------------------

    URL: /api/register
    METHOD: POST

REQUEST HEADERS

| HEADER | VALUE |
| ------ | ----- |
| Content-Type  | application/x-www-form-urlencoded  |

REQUEST PARAMETERS

| PARAM  | TYPE | REQUIRED | EXAMPLE |
| -------| ---- | -------- | ------- |
| name  | String  | yes | fede |
| lastname | String  | yes | jota |
| email  | String  | yes | fede@gmail.com |
| password | String  | yes | Passw0rd! |
| tyc  | Boolean  | yes | true |
| g-captcha-response | String | yes | 12093asihd20 |
| day | Number | yes | 12 |
| month | Number | yes | 1 |
| year | Number | yes | 1999 |
| _csrf | String | yes | 0522f89b-d77c-483e-88ba-f065c486efce |

REQUEST EXAMPLE

POST /api/register HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Content-Length: 1115

name=fede&lastname=jota&_csrf=5081c1fe-b37b-4c1c-807c-36f5289e0a94&email=fede%40gmail.com&tyc=true&g-captcha-response=CAPTCHA&day=18&month=9&year=1990&password=Passw0rd!

Forgot Password
-----------------------

    URL: /api/sendEmailConfirmation
    METHOD: POST

REQUEST HEADERS

| HEADER | VALUE |
| ------ | ----- |
| Content-Type  | application/x-www-form-urlencoded  |

REQUEST PARAMETERS

| PARAM  | TYPE | REQUIRED | EXAMPLE |
| -------| ---- | -------- | ------- |
| username | String  | yes | fede@mail.com |
| g-captcha-response | String | yes | 12093asihd20 |
| _csrf | String | yes | 0522f89b-d77c-483e-88ba-f065c486efce |

REQUEST EXAMPLE

POST /api/sendEmailConfirmation HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Content-Length: 678

username=fede@mail.com&_csrf=5081c1fe-b37b-4c1c-807c-36f5289e0a94&g-captcha-response=CAPTCHA

Reset Password
-----------------------

    URL: /api/sendEmailConfirmation
    METHOD: POST

REQUEST HEADERS

| HEADER | VALUE |
| ------ | ----- |
| Content-Type  | application/x-www-form-urlencoded  |

REQUEST PARAMETERS

| PARAM  | TYPE | REQUIRED | EXAMPLE |
| -------| ---- | -------- | ------- |
| token | String  | yes | 054874ac-cb6b-11e5-9956-625662870761 |
| newPassword | String  | yes | Passw0rd! |
| confirmPassword | String  | yes | Passw0rd! |
| g-captcha-response | String | yes | 12093asihd20 |
| _csrf | String | yes | 0522f89b-d77c-483e-88ba-f065c486efce |

REQUEST EXAMPLE

POST /api/sendEmailConfirmation HTTP/1.1
Content-Type: application/x-www-form-urlencoded
Content-Length: 678

username=fede@mail.com&_csrf=5081c1fe-b37b-4c1c-807c-36f5289e0a94&g-captcha-response=CAPTCHA

Technology Stack
---------------

- [java8](http://docs.oracle.com/javase/8/ "java8")
- [SpringBoot](http://projects.spring.io/spring-boot/ "SpringBoot")
- [gradle](https://gradle.org/ "gradle")
