

# Usage
# For just assessment from filtered logs - ./run_assesment.sh --from-filtered-log
# For generating filtered logs first before assessment - ./run_assesment.sh
if [[ "$1" != "--from-filtered-log" ]] 
then 
    python3 assesment/generateFilteredLogs.py ./chromedriver.log
fi
python3 assesment/localAssesment.py ./filtered_logs.json assesment/AIS_MO_01.json



