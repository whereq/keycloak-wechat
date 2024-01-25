FROM quay.io/keycloak/keycloak:18.0.2

COPY target/keycloak-wechat.jar /opt/keycloak/providers/

CMD ["start-dev", "--hostname-strict=false"]