### Transaction API

결제요청을 받아 카드사와 통신하는 인터페이스를
제공하는 결제시스템

## 개발 프레임워크

- Spring boot


## 테이블 설계

- https://github.com/doublemetal/transaction/issues/8


## 문제해결 전략

인터페이스 관련
- 3개의 API 를 구현한다

- API 서비스로 모든 응답은 정해진 json 형식으로 처리

- 결제 & 조회 API : 
    
    시스템 내에도 결제 정보가 필요할 것이기 때문에 DB에 결제정보를 저장하고, 카드사 API 를 호출(항상 성공)하는 것으로 결정
    
    금액은 정확한 처리를 위해서 BigDecimal 객체를 사용하고, 부가가치세의 자동계산은 11로 나누고, scale 을 0으로 설정
    
    카드정보는 AES 로 암호화하여 저장, 조회할 때 복호화하여 각 정보에 다시 셋팅 
    

- 취소 : 관리번호로 결제된 내역이 있어야되고, 이미 취소한 내역은 원거래의 관리번호로 조회하여 검증

- 부분취소

  취소와 유사하고, 금액과 VAT 별로 모든 취소금액을 합산하여 결제금액과 비교하여 가능여부를 판단


데이터 관련
- 데이터는 H2 DB 로 저장한다
- 20자리의 관리번호를 저장할 수 있는 컬럼 타입을 사용한다
- 450자로 구성된 결제정보는 명시적으로 스펙을 관리한다
- 암/복호화는 간단하게 구현한다


트랜잭션 관련
- 관리번호는 유일성이 보장되어야 한다

  별도의 Sequence 관리용 테이블을 생성 -> 중복 발행 이슈가 간단하게 해결이 되지 않아, Auto increment 필드를 활용하여 날짜와 합쳐 관리번호를 생성하도록 변경, 관리번호를 그냥 sequence 로 해도 되지만, 운영할 때 날짜가 포함되어 있는 편이 더 유용해보인다. 총 20자리에서 날짜(yyyyMMdd)의 자리수 8을 빼면 12 자리이고, 일일 9999억여건의 거래를 수용가능

- 결제 및 취소는 중복 처리가 방지 되어야 한다 (멀티스레드 대응)
  
  중복 체크를 하고, 결제 혹은 결제취소를 시도한다
  

## 빌드 및 실행 방법

Maven 빌드, TransactionApplication 으로 실행
