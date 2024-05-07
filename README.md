# CARPHA-PROJECT  
## Introduction  
The Caribbean Public Health Agency (CARPHA) is a regional Institution of the Caribbean Community (CARICOM), was established on July 4, 2011, through the ratification of an Inter-Governmental Agreement by CARICOM Heads of Government. CARPHA subsumed the functions of the previous five Regional Health Institutions – The Caribbean Epidemiology Centre (CAREC), the Caribbean Food and Nutrition Institute (CFHI), the Caribbean Health Research Council (CHRC), the Caribbean Regional Drug Testing Laboratory (CRDTL) and the Caribbean Environmental Health Institute (CEHI). The Agency began operation in January 2013 with Headquarters in Port of Spain, Trinidad and offices in Saint Lucia and Jamaica. CARPHA serves 26 Member States in the Caribbean Region.  

The objective of implementing a regional integrated public health surveillance system (RIPHSS) is to improve the efficiency and effectiveness of regional public health surveillance by utilising modern technologies for data warehousing, real-time data monitoring and alerts, and integrated data reporting functions. The system is expected to collect, analyse, and visualise health-related data from various sources in real-time, to identify trends, patterns, and outbreaks and facilitate timely decision-making. Finally, reporting and data visualisation tools will be used to present the information in accessible and actionable formats to key stakeholders. The integrated system will enable public health officials to monitor and respond to health threats more efficiently and effectively, ultimately improving public health outcomes.

  
## Project Objectives
The major objective of this repository is to serve as the centre domain for all OpenHIM Mediators for the afroementioned implementation of the RIPHSS interoperability microservices.

## Environment and Code setup  
### Setup  
- Connect to Mirabilia Server linking the OpenHIM node and tunnel ports 8080; 5001; 5000; 9000; to your local instance (preferable using Putty)
- You can also setup a local testing environment on your local machine for this purpose following the guideline on DHIS2, Mongo DB, OpenHIM and HL7 Hapi Websites.

### Code Setup 
**Important**
-  All collaborators are to clone the master template branch into a new branch in the following naming convention i.e. devlevel-cms-country-mediator (e.g master-lmis-barbados-mediator) depending on the system you are adapting.
-  change the urn using the following naming convention urn:mediator:carpha-<cms>-<country>-to-riphssdhis2
-  change the name and the description to suite the purpose of the adapter
-  change the port under the endpoint to a unique port; (this can be obtained from the project lead)
-  note that original passwords should never be stored on the json file or anywhere on this mediator; please use placeholder passwords only. 
