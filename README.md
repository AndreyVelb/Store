# Store

*This project was created with Spring framework using MVC architectural pattern. I tried to follow the rules of 
REST-api construction in it. Interaction with the frontend was organized using json*


### [Entity](https://github.com/AndreyVelb/Store/tree/master/src/main/java/com/velb/shop/model/entity)
- **User**
This entity is projection on **users** table. It includes all data about admins and consumers.

- **Product**
This entity is projection on **users** table. It includes all data about products.
If a product needs to be removed, its quantity becomes 0, and it cannot be found in the search.
This is done because the product is tied to already created orders through the basketElements.

- **BasketElement**
This entity is projection on **basket_elements** table. Each basket contains of basketElements. 
And each order contains of basketElements, but they have field *priceInOrder* in which the purchase price is saved.
It also contains field *productBookingTime*, which indicates the time after the reservation of a certain amount of goods 
when preparing an order layout.

- **Order**
This entity is projection on **orders** table. It includes all data about created orders. 
Field *lastUser* indicates the last user who changed anything in the order, 
and the field *orderStatus* shows what exactly


### [Repository](https://github.com/AndreyVelb/Store/tree/master/src/main/java/com/velb/shop/repository)
They are designed to interact with the database. Each entity has its own repository.
The only exception is the HashtagRepository, it was created to search products by hashtags using the Criteria API.
To search products by request in the ProductRepository, use *FullTextSearch by Postgres*.

### [Service](https://github.com/AndreyVelb/Store/tree/master/src/main/java/com/velb/shop/service)
###### -BasketElementService 
All methods in the service are divided into single and multiple (call single). 
Multiple methods are not transactional and are designed not to roll back all changes due to one exception when calling 
a single method, but to roll back only the action in which there was an exception by writing a message to the consumer.
###### -EmailService 
This service is used to send emails to users in the following cases: creating an order and deleting or changing
the product that is in their basket.
###### -OrderService
This service includes the basic methods of order management as a consumer and administrator. 
###### -ProductService
This service includes all operations for interaction with the products of the administrator.
###### -ProductSearchService
The main method of this service is _findProducts_. It searches for the user's request. If the request contains hashtags,
it searches for them using Criteria API. If the hashtags are not specified in the request, but the search query 
is specified, it searches for it using fulltext search by Postgres. if neither is present, it returns a page with 
all products.
###### -ScheduleService
This service is used in [Scheduler](https://github.com/AndreyVelb/Store/blob/master/src/main/java/com/velb/shop/shedule/ClearingDeferredProductsSchedule.java).
It removes the reservation from the goods that the buyer has booked by preparing the order, but not completing it.
It runs every 3 hours, checks the baskets of users and if there are goods that have been booked
more than 10 minutes ago and the order with them was not completed, increases the total quantity of goods by the amount
that was booked
###### -UserService
This service is used for registration and authentication.
######-MessageCreatorService
This service is used for creation massages.

### [Controller](https://github.com/AndreyVelb/Store/tree/master/src/main/java/com/velb/shop/controller)
###### -BasketController
This controller is used to process customer requests related to his basket.
###### -OrderController
**- for admins:**
* shows the entire order history of a particular user or the entire order history
* allows you to create, modify or delete an order
 
**- for consumers:** 
* prepares an order layout, which means booking the amount of products that the consumer wants to buy or everything that 
is on sale if there is not enough
* creates an order based on a layout
* cancels the creation of the order (if the user does not cancel the reservation, the product will be canceled using the 
scheduler)
###### -ProductController
**- for all:**
* product search

**- for admins:**
* Allows you to create, modify or delete a products
###### -RegistrationController
This controller is used for user registration
