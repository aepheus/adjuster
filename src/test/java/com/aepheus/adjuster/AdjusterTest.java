package com.aepheus.adjuster;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
* Unit test for simple App.
*/
public class AdjusterTest
extends TestCase
{
  /**
  * Create the test case
  *
  * @param testName name of the test case
  */
  public AdjusterTest( String testName )
  {
    super( testName );
  }

  /**
  * @return the suite of tests being tested
  */
  public static Test suite()
  {
    return new TestSuite( AdjusterTest.class );
  }

  public void testApp() throws Exception
  {
    App.main(new String[0]);
  }

  public void testFetchingCampaigns() throws Exception
  {
    Adjuster adjuster = new Adjuster();
    Campaign[] campaigns = adjuster.fetchCampaigns();
    assertEquals(campaigns.length,299);
  }

  public void testFetchingCreatives() throws Exception
  {
    Adjuster adjuster = new Adjuster();
    Creative[] creatives = adjuster.fetchCreatives();
    assertEquals(creatives.length,1338);
  }

  public void testCombiningCreativeAndCampaign() throws Exception
  {
    Adjuster adjuster = new Adjuster();
    Campaign[] campaigns = adjuster.fetchCampaigns();
    Creative[] creatives = adjuster.fetchCreatives();
    assertEquals(campaigns.length,299);
    assertEquals(creatives.length,1338);
    assertEquals(campaigns[0].views,0);
    assertEquals(campaigns[0].clicks,0);
    adjuster.computeViewsAndClickOnCampaigns(campaigns,creatives);
    assertEquals(campaigns.length,299);
    assertEquals(creatives.length,1338);
    assertEquals(campaigns[0].views,233326);
    assertEquals(campaigns[0].clicks,16387);
  }

  public void testSavingToMongo() throws Exception
  {
    Adjuster adjuster = new Adjuster();
    MongoConnection mongo = new MongoConnection();
    mongo.removeCreativesAndCampaigns();
    assertEquals(mongo.countCreatives(),0);
    assertEquals(mongo.countCampaigns(),0);
    Campaign[] campaigns = adjuster.fetchCampaigns();
    Creative[] creatives = adjuster.fetchCreatives();
		mongo.saveCreatives(creatives);
		mongo.saveCampaigns(campaigns);
    assertEquals(mongo.countCreatives(),1338);
    assertEquals(mongo.countCampaigns(),299);
  }

  public void testMongoByParentId() throws Exception
  {
    MongoConnection mongo = new MongoConnection();
		Creative[] creativesForParent = mongo.getCreativesByParentId(27822);
    assertEquals(creativesForParent.length,8);
    assertEquals(creativesForParent[0].parentId,27822);
  }

  public void testMongoViewsClicksAggregation() throws Exception
  {
    MongoConnection mongo = new MongoConnection();
		Campaign[] campaignsWithClicksAndViews = mongo.getCampaignsWithClicksAndViews();
    assertEquals(campaignsWithClicksAndViews.length,299);
    assertEquals(campaignsWithClicksAndViews[0].views,179241);
    assertEquals(campaignsWithClicksAndViews[0].clicks,7881);
  }
}
