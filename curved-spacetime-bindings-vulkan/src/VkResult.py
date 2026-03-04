import csv
with open('E:\\curved-spacetime\\curved-spacetime-vulkan-module\\src\\VkResult.csv') as csvfile:
     vkresult = csv.reader(csvfile, delimiter='|')
     for row in vkresult:
         if len(row)==3: # Skip the first line which uses commas (Why IntelliJ)
             print("if (err == Vulkan." + row[0] + "()){errCode = \"" + row[0] + "\";errMsg = \"" + row[2] + "\";}")