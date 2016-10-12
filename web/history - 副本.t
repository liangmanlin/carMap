<?php
/*
 *
 * To change the template for this generated file go to
 * Window - Preferences - PHPeclipse - PHP - Code Templates
 */
 $ak = "Vb06jHkPmvvc68aVmG2NbpcYIvu6v8OI";
 $service_id = "126200";
 $mcode = "9F:2E:6A:35:E8:06:54:2E:6C:03:51:1C:DD:0B:ED:78:4A:EA:C2:1B;com.example.manlin.carmap";
 $entityName = "myCAMRY";
 
 global $db;
 
 $db = new DB();
 
 $lastLogDate = get_last_log_date($db);
 
 $nowLogDate = get_now_log_date();
 	
 $startTime = $argv[2];
 $endTime = $argv[3];
 
 
  
 $url = "http://api.map.baidu.com/trace/v2/track/gethistory?ak={$ak}&mcode={$mcode}&service_id={$service_id}&entity_name={$entityName}&".
 		"start_time={$startTime}&end_time={$endTime}&page_index=1&page_size=5000";
 		
 $result = @ file_get_contents($url);
 $arr = json_decode($result,true);
 //print_r($arr);
 $points = array_reverse($arr['points']);
 $str = "";
 $len = count($points);
 $time1 = $points[0]['loc_time'];
 for($i=0;$i<$len;$i++){
 	$v = $points[$i];
 	$p = $v['location'];
 	$ltime= $v['loc_time'];
 	$endStr = "\n";
 	if($i<$len-1){
 		if(abs($ltime-$points[$i+1]['loc_time'])>40){
 			$dist = get_dist($time1,$ltime);
 			$t = $ltime-$time1;
 			$time1 = $points[$i+1]['loc_time'];
 			$endStr = ",{$t},{$dist}#";
 		}
 	}else {
 		$dist = get_dist($time1,$ltime);
 		$t = $ltime-$time1;
 		$endStr = ",{$t},{$dist}\n";
 	}
 	$str.=$ltime.",".$p[0].",".$p[1].$endStr;
 	
 }
 $str = trim($str,"\n");
 $str = "<?php\necho \"".$str."\";";
 file_put_contents("test.php",$str);
 
 print_r($arr['start_point']);
 print_r($arr['end_point']);
 
 function get_dist($startTime,$endTime){
	 global $ak,$mcode,$service_id,$entityName;
	 $url = "http://api.map.baidu.com/trace/v2/track/gethistory?ak={$ak}&mcode={$mcode}&service_id={$service_id}&entity_name={$entityName}&".
		 "start_time={$startTime}&end_time={$endTime}&simple_return=2";
	 $result = @ file_get_contents($url);
	 $arr = json_decode($result,true);
	 return $arr['distance']/1000;
 }
 function get_last_log_date($db){
    $result = $db->fetchOne("select * from last_log_date limit 1");
    if($result['date'])
        return $result['date'];
        
    return "2016-10-1";
 }
 
 function get_now_log_date(){
    $time = time();
    return date("Y-m-d",$time-86400);
 }
 //----------------------------------------------------------------------------------
 class DB{
    private $con=null;
    private $query_result = null;
    private $rows;
    public function DB(){
        $this->con = mysql_connect("localhost","root","Tom#Jerry&Z");
        if (!mysql_select_db("car_map", $this->con)) {
            throw new Exception("选择数据库表错误:" . mysql_error());		
        }
    }
    
    public function query($sql)
	{
		$sql = trim($sql);
		if ($sql == '') {
			throw new Exception("SQL语句不能为空");
		}
		
		$result = mysql_query($sql, $this->con);
		if ($result === false) {
			throw new Exception("sql执行出错:" . $sql . "   " . mysql_error());
		}
		$this->query_result=$result;
		return $result;
	}
    public function fetchAll($sql) 
		{
		$this->query($sql);
		return $this->getAll($this->query_result);
		}
	
	public function fetchOne($sql)
	{
		$this->query($sql);
		return $this->getOne($this->query_result);
	}
	
	public function getOne($result) 
	{
		if ($this->query_result) {
			$result =  mysql_fetch_assoc($this->query_result);
			if (!$result) {
				return array();
			}
			return $result;
		}
		throw new Exception("获取sql执行结果出错，可能尚未执行sql");
	}
	
	public function getAll($result) 
	{
		if ($this->query_result) {
			$this->rows = array();
			while (($row = mysql_fetch_assoc($this->query_result)) !== false) {
				array_push($this->rows, $row);
			}
			return $this->rows;
		}
		throw new Exception("获取sql执行结果出错，可能尚未执行sql");
	}
 }
?>
