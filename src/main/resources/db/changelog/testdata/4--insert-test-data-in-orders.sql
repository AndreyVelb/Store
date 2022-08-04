INSERT INTO orders(date, consumer_id, consumer_status, content, total_cost)
VALUES ('2022-05-09 15:11:52.909688',
        2,
        'SENT',
        '[
          {
            "product": {
              "id": 1,
              "title": "Шкаф",
              "description": "Красивый дубовый шкаф из массива дуба",
              "price": 100
            },
            "amount": 3
          },
          {
            "product": {
              "id": 3,
              "title": "Шкаф",
              "description": "Большой платяной шкаф из березы",
              "price": 20
            },
            "amount": 1
          },
          {
            "product": {
              "id": 5,
              "title": "Стол",
              "description": "Небольшой журнальный столик из массива березы",
              "price": 46
            },
            "amount": 2
          }
        ]',
        412);

INSERT INTO orders(date, consumer_id, consumer_status, content, total_cost)
VALUES ('2022-05-09 15:11:52.909688',
        3,
        'IN_PROCESS',
        '[
          {
            "product": {
              "id": 1,
              "title": "Шкаф",
              "description": "Красивый дубовый шкаф из массива дуба",
              "price": 100
            },
            "amount": 1
          }
        ]',
        100);

INSERT INTO orders(date, consumer_id, consumer_status, content, total_cost)
VALUES ('2022-05-09 15:11:52.909688',
        4,
        'SENT',
        '[
          {
            "product": {
              "id": 2,
              "title": "Шкаф",
              "description": "Красивый шкаф из двп отделанный массивом дуба",
              "price": 150
            },
            "amount": 9
          },

          {
            "product": {
              "id": 9,
              "title": "Шкаф",
              "description": "Выполнен из двп, с покрытием из березового шпона",
              "price": 180
            },
            "amount": 8
          }
        ]',
        2790);

INSERT INTO orders(date, consumer_id, consumer_status, content, total_cost)
VALUES ('2022-05-09 15:11:52.909688',
        4,
        'DELIVERED',
        '[
          {
            "product": {
              "id": 2,
              "title": "Шкаф",
              "description": "Красивый шкаф из двп отделанный массивом дуба",
              "price": 150
            },
            "amount": 10
          },
          {
            "product": {
              "id": 6,
              "title": "Стул",
              "description": "Роскошный стул из массива дуба который отлично впишется в любой интерьер",
              "price": 12
            },
            "amount": 1
          },
          {
            "product": {
              "id": 7,
              "title": "Стул",
              "description": "Компьютерный стул. Изготовлен из эргономичного пластика, с тканевыми вставками",
              "price": 10
            },
            "amount": 3
          },
          {
            "product": {
              "id": 9,
              "title": "Шкаф",
              "description": "Выполнен из двп, с покрытием из березового шпона",
              "price": 180
            },
            "amount": 2
          },
          {
            "product": {
              "id": 10,
              "title": "Шкаф",
              "description": "Надежный шкаф, выполнен из пластика. Легко собирается и разбирается",
              "price": 100
            },
            "amount": 6
          }
        ]',
        2502);