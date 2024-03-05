## Deploy to KyeCloak server

### Copy the jar file to Keycloak server:
```shell
$ cp keycloak-wechat*.jar _KEYCLOAK_HOME_/providers/
```
### Copy the theme files to Keycloak server:
```shell
$ mkdir -p _KEYCLOAK_HOME_/themes/base/admin/resources/partials
$ cp templates/realm-identity-provider-weixin.html _KEYCLOAK_HOME_/themes/base/admin/resources/partials
$ cp templates/realm-identity-provider-weixin-ext.html _KEYCLOAK_HOME_/themes/base/admin/resources/partials
```
