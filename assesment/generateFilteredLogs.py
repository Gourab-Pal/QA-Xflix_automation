import json
import sys

# from sqlalchemy import over

def construct_log_json(log_text):
    LOG= {
    "actions":[]
    }
    log_frame=""
    log_object_frame={"COMMAND":None, "RESPONSE":None}
    selector = None
    for line in open(log_text):
        if('[INFO]' in line and 'COMMAND' in line ):
            if ("COMMAND ExecuteScript" not in str(log_object_frame)):
                LOG["actions"].append(log_object_frame)
            selector = "COMMAND"
            log_object_frame={"COMMAND":None, "RESPONSE":None}
            log_frame=""
        elif('[INFO]' in line and 'RESPONSE' in line ):
            selector = "RESPONSE"
        

        if(selector is not None):
            if(line.startswith('[') and log_frame == "" ):
                log_frame = log_frame+line
                log_object_frame[selector]=log_frame
            elif(line.startswith('[') and log_frame != ""):
                log_frame=""
                selector = None
            else:
                log_frame=log_frame+line
                log_object_frame[selector]=log_frame
    json.dump(LOG , open('filtered_logs.json','w+'))
    return LOG

if __name__ == "__main__":
    
    chromedriverlog_path = sys.argv[1] 
    chrome_logs = construct_log_json(chromedriverlog_path)

