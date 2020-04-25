### Transaction API

결제요청을 받아 카드사와 통신하는 인터페이스를
제공하는 결제시스템

## 개발 프레임워크

- Spring boot


## 테이블 설계

- 

## 문제해결 전략

인터페이스 관련
- 3개의 API 를 구현한다

데이터 관련
- 데이터는 H2 DB 로 저장한다
- 20자리의 관리번호를 저장할 수 있는 컬럼 타입을 사용한다
- 450자로 구성된 결제정보는 명시적으로 스펙을 관리한다
- 암/복호화는 간단하게 구현한다


트랜잭션 관련
- 관리번호는 유일성이 보장되어야 한다

  별도의 Sequence 관리용 테이블을 생성

- 결제 및 취소는 중복 처리가 방지 되어야 한다
  
  중복 체크를 하고, 결제 혹은 결제취소를 시도한다
  

## 빌드 및 실행 방법

Maven 빌드, TransactionApplication 으로 실행
