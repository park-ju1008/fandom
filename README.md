# fandom

[작품 설명]

1.앱 이름

 팬덤

 
2. 앱 설명 

  자신이 좋아하는 아이돌의 커뮤니티 앱으로 스타의 오늘 스케줄과 공지사항 소식들과
  팬들 간의 커뮤니티 공간을 제공 하는 앱입니다.

3.주요기능

 (1) 회원 가입
 
    - 프로필 사진을 추가(사진촬영,앨범에서 선택)
    - 구글,페이스북, 카카오로 로그인 가능

   ![Alt Text](https://github.com/park-ju1008/fandom/blob/master/gif/login.gif)



 (2) 스케줄 확인

    - 공식 홈페이지에서 파싱 해온 데이터를 제공
    - 팬이 직접 캘린더에 스케줄 입력 가능
   
   ![Alt Text](https://github.com/park-ju1008/fandom/blob/master/gif/schedule.jpg)
   ![Alt Text](https://github.com/park-ju1008/fandom/blob/master/gif/schedule2.jpg)
   
  (3) 단체채팅

     - netty를 이용한 채팅

     - 실시간으로 여러 사용자들과 대화를 할수 있음.
     
     - 채팅방에서 나갔을 시 FCM을 통하여 알림을 받음.
     
   
   ![Alt Text](https://github.com/park-ju1008/fandom/blob/master/gif/chat.gif)
   
  (4) 라이브 방송

     - nginx rtmp 모듈을 이용한 실시간 방송
     - 방송과 함께 채팅가능

   ![Alt Text](https://github.com/park-ju1008/fandom/blob/master/gif/broadcast.gif)

   ![Alt Text](https://github.com/park-ju1008/fandom/blob/master/gif/broadcast2.gif)


  (5) 쪽지 기능

      - 다른 유저에게 쪽지를 보낼 수 있음.
      - 받은 상대는 FCM을 통해 푸시 알림을 받음.

  (6) 게시글 작성
  
      - 커뮤니티 게시판에 글을 작성 할 수 있음.

   (9) 댓글 및 대댓글

      - 게시판에 작성 된 글에 댓글과 대댓글을 달 수 있으며 삭제 가능.
      
   ![Alt Text](https://github.com/park-ju1008/fandom/blob/master/gif/board.gif)
   

4.주요기능

  Language: PHP, java, HTML
  
  OS: Linux(CentOS) 
  
  Web Server: nginx
  
  Database: MariaDB
  
  library/API: 카카오 로그인API, 구글 로그인API, 페이스북 로그인 API, retrofit2, jsoup, steho, exoplayer, tedPicker, calendar-view
