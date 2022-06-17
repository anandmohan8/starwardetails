package com.anand.crate;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * Main program to find beow details from https://swapi.dev/
 * Find all ships that appeared in Return of the Jedi
 * Find all ships that have a hyperdrive rating >= 1.0
 * Find all ships that have crews between 3 and 100
 */
public class StartWarMetadata {

  public static final String BASE_URL="https://swapi.dev/api/";
  public static final String HTTP_OK="OK";
  public static final String HTTP_NOT_FOUND="404";
  public static final String HTTP_BAD_REQUEST="400";



  public static void main(String[] args) throws Exception {
    System.out.println("Inside Main :::::::");

    JSONArray starShipsArr=new JSONArray();
    ArrayList<String> jediSpaceShips=new ArrayList<>();
    Set<String> allSpaceShips=new HashSet<>();


    /*
     * Initial call to make call to /films endpoint to get the master data and filter data based on input conditions
     */
    HttpResponse<JsonNode> filmsResponse
      = Unirest.get(BASE_URL+"/films")
      .header("accept", "application/json")
      .asJson();

    // retrieve the parsed JSONObject from the response
    if(HTTP_OK.equals(filmsResponse.getStatusText())) {
      JSONObject filmObj = filmsResponse.getBody().getObject();
      System.out.println("filmObj is "+filmObj);
      JSONArray filmResults = filmObj.getJSONArray("results");

      /*
       * allSpaceShips to hold data for all the startships exposed by api
       * jediSpaceShipNames to hold the name of all starShips used in Return of the Jedi movie
       */
      for(int i=0;i<filmResults.length();i++){
        JSONArray spaceShipArray=new JSONArray();
        spaceShipArray= filmResults.getJSONObject(i).getJSONArray("starships");
        if(null!=spaceShipArray && spaceShipArray.length()>0){
          for(int j=0;j<spaceShipArray.length();j++){
            allSpaceShips.add((String)spaceShipArray.get(j));
          }
        }
        if("Return of the Jedi".equals(filmResults.getJSONObject(i).getString("title"))){
          starShipsArr= filmResults.getJSONObject(i).getJSONArray("starships");
        }
      }

    ArrayList<String> jediSpaceShipNames=new ArrayList<>();
    ArrayList<String> spaceShipHyperdriveMoreThan1=new ArrayList<>();
    ArrayList<String> spaceShipCrewBetween3to100=new ArrayList<>();

      /*
       * Populate the name of the starships used in Return of the Jedi movie
       */
      if(null!=starShipsArr && starShipsArr.length()>0){
        for(int i=0;i<starShipsArr.length();i++){
          jediSpaceShipNames.add(getSpaceShipDetails((String)starShipsArr.get(i),"name"));
        }
      }

      /*
       * Populate the values of starships
       * 1. For which hyperdrive_rating >=1.0
       * 2. For which crew count is between 3 and 100
       */
      if(null!=allSpaceShips && allSpaceShips.size()>0){
        String[] allSpaceShipsArr = allSpaceShips.toArray(new String[allSpaceShips.size()]);
        for(int i=0;i<allSpaceShips.size()-1;i++){
          String eligibleHyperDriveShip=getSpaceShipDetails(allSpaceShipsArr[i],"hyperdrive_rating");
          if(null!=eligibleHyperDriveShip){
            spaceShipHyperdriveMoreThan1.add(eligibleHyperDriveShip);
          }
          String eligibleCrewShip=getSpaceShipDetails(allSpaceShipsArr[i],"crew");
          if(null!=eligibleCrewShip){
            spaceShipCrewBetween3to100.add(eligibleCrewShip);
          }
        }
      }

      /*
       * Printing the result of the exercise 1, 2 , 3
       */
      System.out.println("jediSpaceShipNames are : "+jediSpaceShipNames);
      System.out.println("spaceShipHyperdriveMoreThan1 are : "+spaceShipHyperdriveMoreThan1);
      System.out.println("spaceShipCrewBetween3to100 are : "+spaceShipCrewBetween3to100);

    }
    else{
      System.out.println("Film Data Not Found");
    }

  }

/*
  * This method accepts the url to call and Key for which the result set needs to be searched and the result returned
  *
 */
  private static String getSpaceShipDetails(String url,String key){
    HttpResponse<JsonNode> spaceShipResponse = null;
    try {
      spaceShipResponse = Unirest.get(url)
      .header("accept", "application/json")
      .asJson();
    } catch (UnirestException e) {
      e.printStackTrace();
    }
    JSONObject spaceShipObj = spaceShipResponse.getBody().getObject();
    if("name".equalsIgnoreCase(key)) {
      return spaceShipObj.get(key).toString();
    }

    if("hyperdrive_rating".equalsIgnoreCase(key) && !"unknown".equalsIgnoreCase(spaceShipObj.get(key).toString())){
      return Double.valueOf(spaceShipObj.get(key).toString())>=1.0?spaceShipObj.get("name").toString():null;
    }
    if("crew".equalsIgnoreCase(key) && !"unknown".equalsIgnoreCase(spaceShipObj.get(key).toString())){


      if(spaceShipObj.get(key).toString().indexOf("-")<0 && Integer.parseInt(spaceShipObj.get(key).toString().replaceAll(",", ""))>=3 && Integer.parseInt(spaceShipObj.get(key).toString().replaceAll(",", ""))<=100){
        return spaceShipObj.get("name").toString();
      }
      else if(spaceShipObj.get(key).toString().indexOf("-")>=0){
        String [] crewArr=spaceShipObj.get(key).toString().split("-");
        if(Integer.parseInt(crewArr[0])>=3 && Integer.parseInt(crewArr[1])<=100){
          return spaceShipObj.get("name").toString();
        }
      }
    }

    return null;
  }

}
