# BookStoreInventory
Last project for Android Basics Nanodegree by Google. The goal was to design and create the structure of an Inventory App which would allow a store to keep track of its inventory
 Create README.md

In this project, I designed and created the structure of a Inventory App that allow a bookstore to keep track of its inventory of products. The app need to store information about the product and allow the user to track sales, check data and make it easy for the user to order more from the listed supplier. I used two tables of database. One to store data of the books and one for suppliers info. 

## The main screen with the list of all books. 
The sale button decrement the quantity by one without enter in the editor activity.

![Books list](https://www.abicetta.com/abwp/wp-content/uploads/2018/06/BookStoreInventory04M.png)

## The editor screen
Where you can insert a new book or edit the data of an existing one.

![Editor](https://www.abicetta.com/abwp/wp-content/uploads/2018/06/BookStoreInventory07M.png)
![Editor](https://www.abicetta.com/abwp/wp-content/uploads/2018/06/BookStoreInventory09M.png)

## The list of all suppliers. 
If you change the data of a supplier, the related info in the database of books that have that same supplier will also be 
updated automatically.

 ![Suppliers list](https://www.abicetta.com/abwp/wp-content/uploads/2018/06/BookStoreInventory01M.png)

## The check screen. 
If you are not sure if the title of a book or its authors are correct you can check by ISBN code. The app connects to the google books API and finds the corresponding data for that book. You can choose whether to replace your data with those of google API or keep yours. If there is no correspondence in the google API, a message will appear informing you that no books was found by that ISBN code.

 ![Check Screen with no book found](https://www.abicetta.com/abwp/wp-content/uploads/2018/06/BookStoreInventory06M.png)
 ![Check screen](https://www.abicetta.com/abwp/wp-content/uploads/2018/06/BookStoreInventory08M.png)

## Skills
This app use a ContentProvider backed by a SQLite database. This ContentProvider is updated whenever changes are made to the database (CRUD methods). Other skills covered:
-   Storing information in a SQLite database
-   Integrating Android’s file storage systems into that database
-   Setting up a Content Provider and use it to access a repository of data
-   Presenting information from files and SQLite databases to users
-   Updating information based on user input
-   Creating intents to other apps using stored information
-   Connecting to an API
-   Parsing the JSON response

Star my project if you like it 😊
