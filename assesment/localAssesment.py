
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


class logProvider():
    def __init__(self, filtered_logs) -> None:
        self.log = filtered_logs
        self.index = 0
        self.length = filtered_logs.get('actions').__len__()
        self.succesful_node = 0 

    def get_next_log_statement(self):
        if(self.index >= self.length):
            return None
        self.index = self.index+1
        return self.log.get('actions')[self.index-1]
    def set_success_node_index(self):
        self.succesful_node = self.index
    
    def reset_to_last_successful_node(self):
        self.index = self.succesful_node


def batch_validate(log_pair, validations):
    status = []
    for validation in validations:
        if(validate(log_pair.get(validation['object_notation']), validation['operator'], validation['expected_value'])):
            status.append(True)
    return status.__len__()==validations.__len__()


def validate(text, operator, expected_value):
    text = str(text)
    if(operator.lower() == 'contains'):
        return text.__contains__(expected_value)
    elif(operator.lower() == 'equals'):
        return text == expected_value
    elif(operator.lower() == 'notequal'):
        return text != expected_value
    else:
        raise Exception(
            "Invalid Operator {0} in instruction file".format(operator))


def assert_logs(chrome_logs, assesment_instruction_file):
    overall_status = {
        "success":0,
        "failure":0
    }
    final_output={

    }
    if(chrome_logs is None):
        print("assesment ERROR: Unable to detect Logs")
        return
    try:
        assesment_instructions = json.load(open(assesment_instruction_file))['instruction_set']
        assert(assesment_instruction_file != None)
    except Exception as e:
        print("assesment ERROR: invalid assesment file " + str(e))
        return

    for instruction in assesment_instructions:
        Logs = logProvider(chrome_logs)
        final_output[instruction['description']]="TEST_STATUS_FAILURE"
        while True:
            log_item =Logs.get_next_log_statement()
            if(log_item is None):
                print("-"*50)
                print("assesment FAILED: {0}".format(instruction.get('error_out')))
                print("HINT:\n "+str(instruction.get('hint')))
                Logs.reset_to_last_successful_node()
                overall_status['failure']=overall_status['failure']+1
                break
            status = batch_validate(log_item,instruction.get('validations'))
            if(status):
                Logs.set_success_node_index()
                overall_status['success']=overall_status['success']+1
                final_output[instruction['description']]="TEST_STATUS_SUCCESS"
                break
        else:
            final_output[instruction['description']]="TEST_STATUS_SKIPPED"
    print("*"*100)
    print("\nAssessment status : \n Success: {0} \n Failure = {1} \n Total = {2}".format(str(overall_status['success']), str(overall_status['failure']), str(overall_status['success']+overall_status["failure"])))
    json.dump(final_output,open("assesment_result.json",'w+'))

if __name__ == "__main__":
    filtered_json_path = sys.argv[1] 
    assessment_instruction_file = sys.argv[2]

    filtered_logs_json = json.load(open('filtered_logs.json','r'))
    assert_logs(filtered_logs_json , assessment_instruction_file)

