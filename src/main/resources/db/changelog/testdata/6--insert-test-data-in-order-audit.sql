INSERT INTO order_audit(date, consumer_id, order_info, admin_id, admin_status)
VALUES (now(),
        2,
        '{
          "content": [
            {
              "product": {
                "id": 3,
                "title": "Шкаф",
                "description": "Большой платяной шкаф из березы",
                "price": 20
              },
              "amount": 10
            }
          ],
          "totalPrice": 200,
          "status": "SENT"
        }',
        1,
        'CREATED');

INSERT INTO order_audit(date, consumer_id, order_info, admin_id, admin_status)
VALUES (now(),
        3,
        '{
          "content": [
            {
              "product": {
                "id": 1,
                "title": "Шкаф",
                "description": "Красивый дубовый шкаф из массива дуба по приемлемой цене",
                "price": 100
              },
              "amount": 5
            },
            {
              "product": {
                "id": 2,
                "title": "Шкаф",
                "description": "Красивый шкаф из двп отделанный массивом дуба",
                "price": 150
              },
              "amount": 5
            }
          ],
          "totalPrice": 1250,
          "status": "IN_PROCESS"
        }',
        1,
        'CHANGED');

INSERT INTO order_audit(date, consumer_id, order_info, admin_id, admin_status)
VALUES (now(),
        5,
        '{
          "content": [
            {
              "product": {
                "id": 4,
                "title": "Стол",
                "description": "Надежный кухонный стол для всей семьи. К нему отлично может подойти стул из той же коллекции",
                "price": 15
              },
              "amount": 7
            },
            {
              "product": {
                "id": 5,
                "title": "Стол",
                "description": "Небольшой журнальный столик из массива березы",
                "price": 46
              },
              "amount": 15
            }
          ],
          "totalPrice": 795,
          "status": "IN_PROCESS"
        }',
        1,
        'DELETED');

INSERT INTO order_audit(date, consumer_id, order_info, admin_id, admin_status)
VALUES (now(),
        4,
        '{
          "content": [
            {
              "product": {
                "id": 1,
                "title": "Шкаф",
                "description": "Красивый дубовый шкаф из массива дуба по приемлемой цене",
                "price": 100
              },
              "amount": 5
            },
            {
              "product": {
                "id": 2,
                "title": "Шкаф",
                "description": "Красивый шкаф из двп отделанный массивом дуба",
                "price": 150
              },
              "amount": 5
            }
          ],
          "totalPrice": 1250,
          "status": "DELIVERED"
        }',
        1,
        'CHANGED');
