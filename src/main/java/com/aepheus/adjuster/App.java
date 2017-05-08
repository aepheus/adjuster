package com.aepheus.adjuster;

class App {
	public static void main(String[] args) throws Exception {
		Adjuster adjuster = new Adjuster();
		Creative[] creatives = adjuster.fetchCreatives();
		Campaign[] campaigns = adjuster.fetchCampaigns();
		//mutation warning, campaigns changes in this function.
		adjuster.computeViewsAndClickOnCampaigns(campaigns,creatives);
		adjuster.writeCSV(campaigns);
		adjuster.writeHTML(campaigns);

		MongoConnection mongo = new MongoConnection();
    mongo.removeCreativesAndCampaigns();
		mongo.saveCreatives(creatives);
		mongo.saveCampaigns(campaigns);
		Creative[] creativesForParent = mongo.getCreativesByParentId(34867);
		Campaign[] campaignsWithClicksAndViews = mongo.getCampaignsWithClicksAndViews();
	}
}
