# WAS — We All Some of Stars

Java 21과 JavaFX로 만든 천체 관측 사진 데스크톱 펫입니다. 천체 데이터는 JSON으로 관리하고, 웹 소개 페이지는 `website/`에 둡니다.

## 실행

터미널에서 `app` 폴더로 이동한 뒤 실행합니다.

```powershell
cd C:\Users\master\WAS\app
gradle run
```

## 폴더 구성

- `app/src/main/java/` — Java 애플리케이션 코드
- `app/src/main/resources/celestial-bodies.json` — 천체 정보
- `app/src/main/resources/images/` — 현재 앱에서 사용하는 원본 사진
- `app/src/main/resources/animations/` — 현재 앱에서 사용하는 애니메이션
- `website/` — 브라우저로 여는 천체 소개 페이지
- `docs/archive/` — 이전에 만든 이미지와 애니메이션 보관
- `docs/previews/` — 애니메이션 미리보기 이미지
