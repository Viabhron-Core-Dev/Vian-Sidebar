import json

data = ["folder:123:{\"name\":\"New Folder\",\"colorHex\":\"#FF5722\",\"items\":[],\"folderStyle\":0}"]
json_str = json.dumps(data)
print(json_str)

# When we parse this in Android:
# JSONArray(jsonStr).getString(0) will give the exact string back.
