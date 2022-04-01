# Blind Cafe Server

<div align="center">
<img src="https://user-images.githubusercontent.com/59307414/161209758-c93ed073-0c40-479f-ada5-5f417ed4ff83.png" width="300" />
<br>
<a href="www.blindcafe.me/" target="_blank">www.blindcafe.me</a>
</div>

## 프로젝트 개요
| 구분 |내용|
|:------:|-------|
| 기간 |2021.09. ~ 2021.12. / 현재 업데이트 진행 중|
| 구성 |Planner 1명, Designer 1명, Backend 1명, Frontend(AOS, iOS) 2명<br>中 **Backend** 참여|
| 소개 |상대방의 정보를 알지 못한 채 관심사를 바탕으로<br>매칭된 상대방과 3일간 채팅을 진행하는 데이트 매칭 애플리케이션|
<br>

## 프로젝트 소개
- 관심사가 비슷한 사람과 3일간 대화할 수 있는 소셜 어플입니다.
- 대화 시간이 지날수록 기능 제한이 풀리고 3일 후 프로필 교환에 성공할 시 상대방과 추가적으로 대화할 수 있습니다.

> 어둠속의 카페 가보셨나요? 맞은 편 상대의 얼굴도 나이도 알 순 없지만 그저 대화합니다. 블라인드 카페에서 3일간의 대화만으로 설렘을 느끼고 마음에 드는 상대에게만 프로필을 공개하세요.
> - 관심사를 기반으로 상대방과 매칭되고 그 관심사를 기반으로 다양한 대화 토픽이 생성됩니다.
> - 대화 시간이 지날수록 사용할 수 있는 채팅방 내 기능 제한이 해제됩니다.
> - 프로필 공개는 원하는 사람에게만 3일간의 대화로 충분히 상대방을 알아갔을 때 프로필을 공개할 수 있습니다.
> - 3일 간의 대화가 즐거웠거나 상대방이 더욱 궁금하다면 자신의 프로필을 공개하고 상대방과 프로필을 교환하세요. 프로필 교환에 성공하면 추가적인 대화를 이어 할 수 있습니다.
> - 프로필 교환에 성공할 경우 입장 시 선택했던 음료수의 뱃지를 획득할 수 있습니다.
> - 상대방과 인연이 아니라고 생각이 드시나요? '그만 연락하고 싶어요'란 말을 하지 못해 애매하게 대화를 이어간 적 있나요? 저희 카페에서는 대화를 끝내고 싶은 이유를 선택하고 방을 나가기만 하면 됩니다.
    <br>

## 사용 기술
### Application
- Java 11
- SpringBoot, Spring MVC, Spring Data JPA, Spring Security, Spring REST Docs
- WebSocket, STOMP, SockJS
- Thymeleaf

### Infrastructure
- MySQL
- Redis (Cache, Message Broker)
- Mongo DB
- AWS EC2, RDS, S3, CloudFront, ElastiCache
- Mongo DB Atlas
- Firebase Cloud Messaging
- Nginx
  <br>

## 구성도
> *작성 중입니다.*
<br>

## 기능 설명
> *작성 중입니다.*
<br>

## 배포
### 환경
- Gradle 7.x
- JDK 11
- SpringBoot 2.5.5

### 추가 설정
- `/src/main/resources`에 아래 양식을 이용하여 `application.yml`파일을 작성합니다.
    <details>
    <summary>Sample</summary>
    <div markdown="1">  

    ```yml
    spring:
      profiles:
        active: {적용 환경}
    
    ---
    
    spring:
      profiles:
        group:
          "local": "local, common"
          "prod": "prod, common"
    
    ---
    
    spring:
      config:
        activate:
          on-profile: local
      datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: {Local RDBMS(MySQL) URL}
        username: {Local RDBMS(MySQL) Username}
        password: {Local RDBMS(MySQL) 비밀번호}
      redis:
        host: {Local Redis URL}
        port: {Local Redis PORT}
      data:
        mongodb:
          uri: {Local Mongo DB URL}
    
    ---
    
    spring:
      config:
        activate:
          on-profile: prod
      datasource:
        driver-class-name: com.mysql.cj.jdbc.Driver
        url: {Prod RDBMS(MySQL) URL}
        username: {Prod RDBMS(MySQL) Username}
        password: {Prod RDBMS(MySQL) 비밀번호}
      redis:
        host: {Prod Redis URL}
        port: {Prod Redis PORT}
      data:
        mongodb:
          uri: {Prod Mongo DB URL}
    
    ---
    
    server:
      port: 8080
      tomcat:
        uri-encoding: UTF-8
    
    spring:
      config:
        activate:
          on-profile: common
      mvc:
        static-path-pattern: /static/**
      jpa:
        database: mysql
        database-platform: org.hibernate.dialect.MySQL5InnoDBDialect
        generate-ddl: true
      servlet:
        multipart:
          max-file-size: 10MB
      mail:
        host: smtp.gmail.com
        port: 587
        username: {Gmail SMTP 이용할 계정의 이메일 주소}
        password: {Gmail SMTP 이용할 계정의 이메일의 비밀번호}
        properties:
          mail:
            smtp:
              starttls:
                enable: true
                required: true
              auth: true
      thymeleaf:
        cache: false
    
    cloud:
      aws:
        credentials:
          accessKey: {AWS Credential Access Key}
          secretKey: {AWS Credential Secret Key}
        s3:
          bucket: {AWS S3 Bucket 이름}
        region:
          static: ap-northeast-2
        cloudfront:
          url: {AWS CloudFront URL}
        stack:
          auto: false
    
    email:
      from: {건의사항 발신 이메일 주소}
      to: {건의사항 수신 이메일 주소}
    
    fcm:
      key: {Firebase Key(json) 파일 경로}
      auth: https://www.googleapis.com/auth/cloud-platform
      api: {FCM Send API URL}
      firebase-create-scoped: https://www.googleapis.com/auth/firebase.messaging
      firebase-multicast-message-size: 450
    
    secret:
      key1: {JWT 시그니처 1}
      key2: {JWT 시그니처 2}
      key3: {JWT 시그니처 3}
    ```
</div>
</details>

- `/src/main/resources/firebase`에 Firebase Key 파일 `blind-cafe-firebase-key.json`을 추가합니다.
<br>

## 결과물
> *작성 중입니다.*
<br>

## 회고
> *작성 중입니다.*
<br>

## 관련
### Team
|Position|Name|Repository|
|:---:|:---:|---|
|Planner|이혜지||
|Designer|김다은||
|Server|[김희동](https://sulky-branch-08e.notion.site/Heedong-Kim-f0962ce4ba2947f68ffb3c3815846f80)|https://github.com/Blind-Cafe/BlindCafe-Server|
|AOS|[노소래](https://github.com/nosorae)|https://github.com/Blind-Cafe/BlindCafe-AOS|
|iOS|[권하은](https://github.com/eilyri)|https://github.com/Blind-Cafe/BlindCafe-iOS|

### Reference
> *작성 중입니다.*