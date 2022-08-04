INSERT INTO users (last_name, first_name, middle_name, email, password, role)
VALUES ('Админ', 'Админ', 'Админ', 'admin@mail.ru', '{bcrypt}$2a$10$jVVC38nBHP0XNEFX12NpSeNKILAEoyuq.JD22gFHMzPEFi6/5Puhm', 'ADMIN'); /*password*/

INSERT INTO users (last_name, first_name, middle_name, email, password, role)
VALUES ('Петров', 'Петр', 'Петрович', 'petrov@gmail.com', '{bcrypt}$2a$10$LF5QBMgB/pCJzLMeoRR1/.pcpnUpAjGLN7PlhbZlaxU3ZStep32T6', 'CONSUMER'); /*petrov*/

INSERT INTO users (last_name, first_name, middle_name, email, password, role)
VALUES ('Иванов', 'Иван', 'Иванович', 'ivanov@yandex.com', '{bcrypt}$2a$10$s9.Vc.L6s8FcPcmoOyeEOOAEPZFZWf2k7IqQWeqQNoLhe45UE1y0i', 'CONSUMER'); /*ivanov*/

INSERT INTO users (last_name, first_name, middle_name, email, password, role)
VALUES ('Андреев', 'Андрей', 'Андреевич', 'andreev@yandex.ru', '{bcrypt}$2a$10$lKZm7M8yW7YgOqkl5gPNd.qD3TEho64f3QStNb8CNXMWsGfj6IfiC', 'CONSUMER'); /*andreev*/

INSERT INTO users (last_name, first_name, middle_name, email, password, role)
VALUES ('John', 'Smith', '', 'john123smith@mail.ru', '{bcrypt}$2a$10$tAb5kMWxNetivbLjqrivWOPZluTG1U4VSMWXEP67FNn0c6IFTBQ/e', 'CONSUMER'); /*johnsmith*/
