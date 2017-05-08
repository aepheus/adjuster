package com.aepheus.adjuster;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileWriter;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import com.google.gson.Gson;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVFormat;

class Adjuster {
	private Gson gson = new Gson();

	void computeViewsAndClickOnCampaigns(Campaign[] campaignList, Creative[] creativeList) {
		HashMap campaignMap = new HashMap();

		for(int i = 0; i < campaignList.length; i++){
			campaignMap.put(campaignList[i].id, campaignList[i]);
		}
		for(int i = 0; i < creativeList.length; i++){
			Campaign c = (Campaign)campaignMap.get(creativeList[i].parentId);
			if(c != null){
				c.views += creativeList[i].views;
				c.clicks += creativeList[i].clicks;
			}
			else{
				//error? we have a creative without a parent campaign
			}
		}
	}

	Campaign[] fetchCampaigns() throws IOException {
		String campaignRes = request("http://homework.ad-juster.com/api/campaign");
		return gson.fromJson(campaignRes, Campaign[].class);
	}

	Creative[] fetchCreatives() throws IOException {
		String creativeRes = request("http://homework.ad-juster.com/api/creative");
		return gson.fromJson(creativeRes, Creative[].class);
	}

	void writeCSV(Campaign[] campaignList) throws IOException {
		String fileName = "list.csv";
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		CSVPrinter printer = new CSVPrinter(writer, CSVFormat.EXCEL);
		for(int i = 0; i < campaignList.length; i++){
			printer.print(campaignList[i].startDate);
			printer.print(campaignList[i].cpm);
			printer.print(campaignList[i].id);
			printer.print(campaignList[i].name);
			printer.print(campaignList[i].clicks);
			printer.print(campaignList[i].views);
			printer.println();
		}
		printer.close();
	}

	void writeHTML(Campaign[] campaignList) throws IOException {
		String fileName = "list.html";
		PrintWriter writer = new PrintWriter(fileName, "UTF-8");
		writer.print("<html><head></head><body><table>");
		for(int i = 0; i < campaignList.length; i++){
			writer.print("<tr>");
			writer.print("<td>"+campaignList[i].startDate+"</td>");
			writer.print("<td>"+campaignList[i].cpm+"</td>");
			writer.print("<td>"+campaignList[i].id+"</td>");
			writer.print("<td>"+campaignList[i].name+"</td>");
			writer.print("<td>"+campaignList[i].clicks+"</td>");
			writer.print("<td>"+campaignList[i].views+"</td>");
			writer.print("</tr>");
		}
		writer.print("</table></body></html>");
		writer.close();
	}

	String request(String url) throws IOException {
		StringBuilder result = new StringBuilder();
		URL urlObj = new URL(url);
		HttpURLConnection creativeConnection = (HttpURLConnection)urlObj.openConnection();
		creativeConnection.setRequestMethod("GET");
		BufferedReader buffer = new BufferedReader(new InputStreamReader(creativeConnection.getInputStream()));
		String line;
		while ((line = buffer.readLine()) != null) {
			result.append(line);
		}
		buffer.close();
		return result.toString();
	}
}
