CREATE TABLE chat_group
(
  group_id          VARCHAR(127)             NOT NULL,
  group_name        VARCHAR(255)             NOT NULL,
  group_description VARCHAR(1000)            NOT NULL,
  group_avatar      VARCHAR(255),
  created_at        TIMESTAMP NOT NULL,
  CONSTRAINT pk_chat_group PRIMARY KEY (group_id)
);

CREATE TABLE chat_role
(
  role_id          VARCHAR(100) NOT NULL,
  role_description VARCHAR(1000),
  CONSTRAINT pk_chatrole PRIMARY KEY (role_id)
);

CREATE TABLE chat_user
(
  user_id         VARCHAR(127)             NOT NULL,
  full_name       VARCHAR(255),
  user_login_type VARCHAR(50)              NOT NULL,
  password        VARCHAR(255),
  avatar          VARCHAR(255),
  created_at      TIMESTAMP NOT NULL,
  CONSTRAINT pk_chat_user PRIMARY KEY (user_id)
);

CREATE TABLE chat_join_record
(
  joined_at TIMESTAMP NOT NULL,
  role_id   VARCHAR(100),
  group_id  VARCHAR(127)             NOT NULL,
  user_id   VARCHAR(127)             NOT NULL,
  CONSTRAINT pk_chatjoinrecord PRIMARY KEY (group_id, user_id)
);

ALTER TABLE chat_join_record
  ADD CONSTRAINT FK_CHATJOINRECORD_ON_GROUP FOREIGN KEY (group_id) REFERENCES chat_group (group_id);

ALTER TABLE chat_join_record
  ADD CONSTRAINT FK_CHATJOINRECORD_ON_ROLEID FOREIGN KEY (role_id) REFERENCES chat_role (role_id);

ALTER TABLE chat_join_record
  ADD CONSTRAINT FK_CHATJOINRECORD_ON_USER FOREIGN KEY (user_id) REFERENCES chat_user (user_id);
