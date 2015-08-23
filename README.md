
     ____        _         ____                                     _
    |  _ \  __ _| |_ __ _ / ___| ___  __  __ __  __  __ _ __  _  __| |_ __ 
    | | | |/ _` | __/ _` | |    / _ \|  `´  |  `´  |/ _` |  \| |/ _  | '__/
    | |_| | (_| | || (_| | |___| (_| | |\/| | |\/| | (_| |   ' | (_| | |
    |____/ \__,_|\__\__,_|\____|\___/|_|  |_|_|  |_|\__,_|_|\__|\__,_|_|

	 I N T E G R A T E.          T R A N S F O R M.          A N A L Y Z E.

# DataCommandr: Integrate. Transform. Analyze. 

DataCommandr Engine is an open source Java library for data integration, transformation and analysis. DataCommandr allows for loading data from different data sources and then defining (potentially highly complicated) data mashups. By a data mashup we mean a new data that is derived from the input data sources by applying various transformations. In this sense, it pursues the same goal as conventional query languages (like SQL) and data transformation languages (like Pig Latin). The unique distinguishing feature is that DataCommander applies a novel column-oriented approach where new data columns are defined in terms of other data columns. It is opposed to most other approaches where new data tables are defined in terms of other data tables. New columns in DataCommander are defined using a novel concept-oriented expression language (COEL). 

## Features 

DataCommander has the following distinguishing features: 

* DataCommandr is based on a novel concept-oriented data model (COM) which is a unified model aimed at generalizing existing data models and data modeling techniques. 

* DataCommandr uses a novel concept-oriented expression language (COEL) for defining new tables and columns in terms of existing tables and columns. 

* DataCommandr uses a column-oriented approach where the main unit of data is a column and every column has its own definition in terms of other columns. 
Data processing is also performed column-wise rather than row-wise in most other systems. 

* DataCommandr uses an internal in-memory data processing engine which is optimized for fast computations. 
It loads data into memory and queries it repeatedly column-wise much faster than in disk-based Hadoop MapReduce or Spark.  

* DataCommandr evaluates data in columns by using dependencies derived from column definitions. It makes it similar to functional approach because the whole model is represented as a number of column and table definitions depending on each other and evaluating a column triggers computations of other columns. 

* DataCommandr can be viewed as an analogue of classical spreadsheets where columns are defined in terms of other columns (in different tables) rather than cells being defined in terms of other cells. 

## More Information

More information about DataCommandr and the underlying data model can be found from the following sources: 

* Examples of using DataCommandr can be found in the `org.conceptoriented.dc.examples` package. The corresponding data sets are located in the resources folder: `/dce-java/src/test/resources` 

* Source code of this project can be checked out from bitbucket: <http://bitbucket.org/conceptoriented/dce-java>

* More information information on all aspects of concept-oriented paradigm including the concept-oriented model and concept-oriented programming including publications can found on the concept-oriented portal: <http://www.conceptoriented.org>

* Concept-oriented expression language (COEL) is a formula language which is used in DataCommandr to define new data columns in terms of other data columns: <http://conceptoriented.org/wiki/Concept-oriented_expression_language>

* COEL can be used to define various types of columns like calculated columns, link columns (used instead of joins), accumulated columns (substitute for group-by). Some examples of these formulas can be found here: <http://conceptoriented.org/wiki/COEL_formulas>

* DataCommandr Engine has been also implemented in C# which is used as a data processing core in the DataCommandr interactive application (written in WPF). It is a self-service tool for agile data transformations. More information about this tool can be found here: <http://www.conceptoriented.com>

* Alexandr Savinov is an author of DataCommander Engine Java library as well as the underlying concept-oriented model (COM): <http://conceptoriented.org/savinov>

## License

[Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)
