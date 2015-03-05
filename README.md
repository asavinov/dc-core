
     ____        _         ____                                     _
    |  _ \  __ _| |_ __ _ / ___| ___  __  __ __  __  __ _ __  _  __| | ___  _ __ 
    | | | |/ _` | __/ _` | |    / _ \|  `´  |  `´  |/ _` |  \| |/ _  |/ _ \| '__/
    | |_| | (_| | || (_| | |___| (_| | |\/| | |\/| | (_| |   ' | (_| |  __/| |
    |____/ \__,_|\__\__,_|\____|\___/|_|  |_|_|  |_|\__,_|_|\__|\__,_|\___/|_|

	 I N T E G R A T E.            T R A N S F O R M.            A N A L Y Z E.

# DataCommander: Integrate. Transform. Analyze. 

DataCommander is a Java library for data integration, transformation and analysis. 

DataCommander is based on a novel approach to data modeling, called the concept-oriented model of data (COM). This model has been developed by Alexandr Savinov and more information about it can including publications be found on [this site](http://conceptoriented.org). 

DataCommander allows for loading data from different sources and then defining (potentially highly complicated) data mashups. By a data mashup we mean a new data that is derived from the input data sources by applying various transformations. In this sense, it pursues the same goal as conventional query languages (like SQL) and data transformation languages (like Pig Latin). The following features either distinguish DataCommander from other approaches or make it unique: 

* DataCommander is based on a novel concept-oriented data model (COM) which is a unified model generalizing many existing data models and data modeling techniques. 

* DataCommander uses a novel concept-oriented expression language (COEL) for defining new tables and column in terms of existing tables and columns. 

* DataCommander uses a column-oriented approach where the main unit of data is a column and every column has a definition in terms of other columns. 
Data processing is also performed column-wise rather than row-wise in most other systems. 

* DataCommander uses an internal in-memory data processing engine which is optimized for fast computations. 
It loads data into memory and queries it repeatedly column-wise much faster than in disk-based Hadoop MapReduce or Spark.  

* DataCommander evaluates data in columns by using dependencies derived from column definitions. It makes it similar to functional approach because the whole model is represented as a number of column and table definitions depending on each other and evaluating a column triggers computations of other columns. 

* DataCommander can be viewed as an analogue of classical spreadsheets where columns are defined in terms of other columns (in different tables) rather than cells being defined in terms of other cells. 

## More Information

More information about Data Commander can be found on the project web site: <http://www.conceptoriented.com>.

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
