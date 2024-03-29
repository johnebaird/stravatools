# Stravatools

## Java and Spring Boot application to provide some useful features for Strava that aren't currently implemented on the website

### Features: 
- Default Indoor and Outdoor bikes and ability to change a range of activities to that outdoor or indoor bike 
- Maintenance reminder to do certain actions every X kilometers (eg: wax your chain)
- Mute activities: mute certain activities from the feed by default by type of activity or duration, eg: mute all walking/yoga or activities <5min

Not currently hosted anywhere will need to self host to use

### Install instructions:
- Sign up for Strava API and copy application id and secret information
- Sign up for Astra DB or host your own cassandra instance
- Sign up for Sendmail if you want maintenance notifications
- set STRAVA_CLIENT_ID,STRAVA_CLIENT_SECRET,CASSANDRA_USERNAME,CASSANDRA_PASSWORD, etc.. as environment variables or set hardcoded into application.yaml
- run maven to install dependencies and run application: *mvnw clean install spring-boot:run*
- access stravatools at http://localhost:8080 and register and link strava account