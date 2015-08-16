
     ____        _         ____                                     _
    |  _ \  __ _| |_ __ _ / ___| ___  __  __ __  __  __ _ __  _  __| |_ __ 
    | | | |/ _` | __/ _` | |    / _ \|  `´  |  `´  |/ _` |  \| |/ _  | '__/
    | |_| | (_| | || (_| | |___| (_| | |\/| | |\/| | (_| |   ' | (_| | |
    |____/ \__,_|\__\__,_|\____|\___/|_|  |_|_|  |_|\__,_|_|\__|\__,_|_|

	 I N T E G R A T E.          T R A N S F O R M.          A N A L Y Z E.

# DataCommandr: Integrate. Transform. Analyze. 

DataCommandr is a Java library for data integration, transformation and analysis. 

DataCommandr is based on a novel approach to data modeling, called the concept-oriented model of data (COM). This model has been developed by [Alexandr Savinov](http://conceptoriented.org/savinov) and more information about it including publications can be found on [this site](http://conceptoriented.org).

DataCommandr allows for loading data from different sources and then defining (potentially highly complicated) data mashups. By a data mashup we mean a new data that is derived from the input data sources by applying various transformations. In this sense, it pursues the same goal as conventional query languages (like SQL) and data transformation languages (like Pig Latin). The following features either distinguish DataCommandr from other approaches or make it unique: 

* DataCommandr is based on a novel concept-oriented data model (COM) which is a unified model aimed at generalizing existing data models and data modeling techniques. 

* DataCommandr uses a novel concept-oriented expression language (COEL) for defining new tables and columns in terms of existing tables and columns. 

* DataCommandr uses a column-oriented approach where the main unit of data is a column and every column has its own definition in terms of other columns. 
Data processing is also performed column-wise rather than row-wise in most other systems. 

* DataCommandr uses an internal in-memory data processing engine which is optimized for fast computations. 
It loads data into memory and queries it repeatedly column-wise much faster than in disk-based Hadoop MapReduce or Spark.  

* DataCommandr evaluates data in columns by using dependencies derived from column definitions. It makes it similar to functional approach because the whole model is represented as a number of column and table definitions depending on each other and evaluating a column triggers computations of other columns. 

* DataCommandr can be viewed as an analogue of classical spreadsheets where columns are defined in terms of other columns (in different tables) rather than cells being defined in terms of other cells. 

## More Information

More information about Data Commandr can be found on the concept-oriented portal: <http://www.conceptoriented.org>.

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
