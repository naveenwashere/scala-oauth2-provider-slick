# scala-oauth2-provider example with Slick

- [scala-oauth2-provider](https://github.com/nulab/scala-oauth2-provider) 0.17.x
- [Play Framework](https://www.playframework.com/) 2.5.x
- [Slick-FRM](http://slick.lightbend.com/) 3.1.x

Inspired from [tsuyoshizawa's](https://github.com/tsuyoshizawa) [implementation](https://github.com/tsuyoshizawa/scala-oauth2-provider-example-skinny-orm) of the oauth2 server using Skinny ORM. I instead wanted to use Slick Functional Relational Mapper to leverage the performance advantages that it provided.

## Running Play Framework with evolutions

```
$ activator run
```

## Try to create access tokens using curl

### Client credentials

```
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=bob_client_id" -d "client_secret=bob_client_secret" -d "grant_type=client_credentials"
```

### Authorization code

```
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id" -d "client_secret=alice_client_secret" -d "redirect_uri=http://localhost:3000/callback" -d "code=bob_code" -d "grant_type=authorization_code"
```

NOTE: A service needs to generate `code` in advance. In this example, the code has been inserted in database by evolutions.

### Password

```
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "username=alice@example.com" -d "password=522b276a356bdf39013dfabea2cd43e141ecc9e8" -d "grant_type=password"
```

### Refresh token

```
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "refresh_token=${refresh_token}" -d "grant_type=refresh_token"
```

NOTE: `${refresh_token}` is you got `refresh_token` from json of password grant. (client_id and client_secret are also same with password grant)

### Access resource using access_token

You can access application resource using access token.

```
$ curl --dump-header - -H "Authorization: Bearer ${access_token}" http://localhost:9000/resources
```

In this example, server just returns authorized user information.

```
HTTP/1.1 200 OK
Content-Type: application/json; charset=utf-8
Content-Length: 90

{"account":{"email":"alice@example.com"},"clientId":"alice_client_id2","redirectUri":null}
```
### Various test scenarios and its results

```
naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=bob_client_id" -d "client_secret=bob_client_secret" -d "grant_type=client_credentials"
{"token_type":"Bearer","access_token":"PoocGtqXVXfTC37pZ0Ls2EBO8uoK13UclN3Af8pL","expires_in":3599,"refresh_token":"LHE4IL7xT6AmBAN7VST0p2KCX5vAVirs3AHp1YWE"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=bob_client_id" -d "client_secret=bob_client_secret" -d "grant_type=client_credential"
{"error":"unsupported_grant_type","error_description":"client_credential is not supported"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=bob_client_id" -d "client_secret=bob_client_secre" -d "grant_type=client_credentials"
{"error":"invalid_client","error_description":"Invalid client is detected"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=bob_client_i" -d "client_secret=bob_client_secret" -d "grant_type=client_credentials"
{"error":"invalid_client","error_description":"Invalid client is detected"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id" -d "client_secret=alice_client_secret" -d "redirect_uri=http://localhost:3000/callback" -d "code=bob_code" -d "grant_type=authorization_code"
{"token_type":"Bearer","access_token":"VajDjkTATBv0ogOyDLNe2HPozSTCrkaNCQFKVzDh","expires_in":3599,"refresh_token":"XgCDuSfTnAF4N022yV6tzY7MeD85VKkqZSIl6rcK"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id" -d "client_secret=alice_client_secret" -d "redirect_uri=http://localhost:3000/callback" -d "code=bob_code" -d "grant_type=authorization_cod"
{"error":"unsupported_grant_type","error_description":"authorization_cod is not supported"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id" -d "client_secret=alice_client_secret" -d "redirect_uri=http://localhost:3000/callback" -d "code=bob_cod" -d "grant_type=authorization_code"
{"error":"invalid_grant","error_description":"Authorized information is not found by the code"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id" -d "client_secret=alice_client_secre" -d "redirect_uri=http://localhost:3000/callback" -d "code=bob_code" -d "grant_type=authorization_code"
{"error":"invalid_client","error_description":"Invalid client is detected"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_i" -d "client_secret=alice_client_secret" -d "redirect_uri=http://localhost:3000/callback" -d "code=bob_code" -d "grant_type=authorization_code"
{"error":"invalid_client","error_description":"Invalid client is detected"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "username=alice@example.com" -d "password=522b276a356bdf39013dfabea2cd43e141ecc9e8" -d "grant_type=password"
{"token_type":"Bearer","access_token":"CCWd7AGHeEeV3YNOsBhKSK4OZw5DoudyWWQDRkvZ","expires_in":3599,"refresh_token":"gymRoFktrUSYOmVCE0EwoZKENLDCq4CaxaXQG03O"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "username=alice@example.com" -d "password=522b276a356bdf39013dfabea2cd43e141ecc9e8" -d "grant_type=password"
{"error":"unsupported_grant_type","error_description":"passwor is not supported"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "username=alice@example.com" -d "password=522b276a356bdf39013dfabea2cd43e141ecc9e" -d "grant_type=password"
{"error":"invalid_grant","error_description":"username or password is incorrect"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "username=alice@example.co" -d "password=522b276a356bdf39013dfabea2cd43e141ecc9e8" -d "grant_type=password"
{"error":"invalid_grant","error_description":"username or password is incorrect"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret" -d "username=alice@example.com" -d "password=522b276a356bdf39013dfabea2cd43e141ecc9e8" -d "grant_type=password"
{"error":"invalid_client","error_description":"Invalid client is detected"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id" -d "client_secret=alice_client_secret2" -d "username=alice@example.com" -d "password=522b276a356bdf39013dfabea2cd43e141ecc9e8" -d "grant_type=password"
{"error":"invalid_client","error_description":"Invalid client is detected"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "refresh_token=${refresh_token}" -d "grant_type=refresh_token"
{"error":"unsupported_grant_type","error_description":"refresh_token is not supported"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "refresh_token=${refresh_token}" -d "grant_type=refresh_token"
{"error":"invalid_grant","error_description":"Authorized information is not found by the refresh token"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret2" -d "refresh_token=gymRoFktrUSYOmVCE0EwoZKENLDCq4CaxaXQG03O" -d "grant_type=refresh_token"
{"token_type":"Bearer","access_token":"cxYKczpkN1X7idW0LMF5hfXvAgOAATRA0psu3lQ7","expires_in":3599,"refresh_token":"dGvGzuITOEO08DSGMQIBuIePwAGI768eNb9LcgD5"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id2" -d "client_secret=alice_client_secret" -d "refresh_token=gymRoFktrUSYOmVCE0EwoZKENLDCq4CaxaXQG03O" -d "grant_type=refresh_token"
{"error":"invalid_client","error_description":"Invalid client is detected"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl http://localhost:9000/oauth/access_token -X POST -d "client_id=alice_client_id" -d "client_secret=alice_client_secret2" -d "refresh_token=gymRoFktrUSYOmVCE0EwoZKENLDCq4CaxaXQG03O" -d "grant_type=refresh_token"
{"error":"invalid_client","error_description":"Invalid client is detected"}

naveenkumar at Naveens-MacBook-Pro in ~
$ curl --dump-header - -H "Authorization: Bearer ${access_token}" http://localhost:9000/resources
HTTP/1.1 400 Bad Request
WWW-Authenticate: Bearer error="invalid_request", error_description="Access token is not found"
Content-Length: 0
Date: Wed, 11 Jan 2017 03:22:36 GMT

naveenkumar at Naveens-MacBook-Pro in ~
$ curl --dump-header - -H "Authorization: Bearer CCWd7AGHeEeV3YNOsBhKSK4OZw5DoudyWWQDRkvZ" http://localhost:9000/resources
HTTP/1.1 401 Unauthorized
WWW-Authenticate: Bearer error="invalid_token", error_description="The access token is not found"
Content-Length: 0
Date: Wed, 11 Jan 2017 03:23:12 GMT

naveenkumar at Naveens-MacBook-Pro in ~
$ curl --dump-header - -H "Authorization: Bearer cxYKczpkN1X7idW0LMF5hfXvAgOAATRA0psu3lQ7" http://localhost:9000/resources
HTTP/1.1 200 OK
Content-Length: 90
Content-Type: application/json
Date: Wed, 11 Jan 2017 03:44:34 GMT

{"account":{"email":"alice@example.com"},"clientId":"alice_client_id2","redirectUri":null}
```
