# Stravatools

## Java and Spring Boot application to provide some useful features for Strava that aren't currently implemented on the website

### Features: 
- Default Indoor and Outdoor bikes
- Maintenance reminder to do certain actions every X kilometers (eg: wax your chain) - Not yet implemented
- Mute activities: mute certain activities from the feed by default by type or duration - Not yet implemented

Not currently hosted anywhere will need to self host to use

### Install instructions:
- Sign up for Strava API and copy application id and secret information
- Sign up for Astra DB or host your own cassandra instance
- copy application.yaml.template to application.yaml in ./src/main/resources
- fill out relevent strava and astra information in application.yaml
- run maven to install dependencies and run application: *mvnw clean install spring-boot:run*
- access stravatools at http://localhost:8080 and register and link strava account