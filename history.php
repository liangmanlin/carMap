<?php
define('DS',86400).
include "db.class.php";
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

$time1 = strtotime($nowLogDate." 4:00:00");
$time2 = strtotime($lastLogDate." 4:00:00");

if($time1>$time2){
	$dc = $time1-$time2;
	$day = (int)($dc/DS);
	for($i=0;$i<$day;$i++){
		$startTime = $time2+($i+1)*DS;
		$endTime = $startTime+DS;
		get_history($startTime,$endTime);
	}
}

function get_history($startTime,$endTime){ 	
	global $ak,$mcode,$service_id,$entityName;  
	$url = "http://api.map.baidu.com/trace/v2/track/gethistory?ak={$ak}&mcode={$mcode}&service_id={$service_id}&entity_name={$entityName}&".
		"start_time={$startTime}&end_time={$endTime}&page_index=1&page_size=5000";
	
	$result = @ file_get_contents($url);
	$arr = json_decode($result,true);
	//print_r($arr);
	$points = array_reverse($arr['points']);
	$len = count($points);
	$time1 = $points[0]['loc_time'];
	$totalDist = $arr['distance']/1000;
	
	$Table = 'history_'.date("Y_m",$startTime); 
	check_table($Table); 
	
	$tmpArr = array();
	for($i=0;$i<$len;$i++){
		$v = $points[$i];
		$p = $v['location'];
		$ltime= $v['loc_time'];
		if($i<$len-1){
			if(abs($ltime-$points[$i+1]['loc_time'])>40){
				$dist = get_dist($time1,$ltime);
				$t = $ltime-$time1;
				$time1 = $points[$i+1]['loc_time'];
				$tmpArr[] = array($ltime,$p[0],$p[1],$t,$dist);
			}else{
				$tmpArr[] = array($ltime,$p[0],$p[1]);
			}
		}else {
			$dist = get_dist($time1,$ltime);
			$t = $ltime-$time1;
			$tmpArr[] = array($ltime,$p[0],$p[1],$t,$dist);
		}
		if(($i+1)%500 == 0){
			dump_data($Table,$tmpArr);
			$tmpArr = array();
		}
	}
	if(count($tmpArr)>0){
		dump_data($Table,$tmpArr);
	}
	log_dist($startTime,$totalDist);
	log_log_date($startTime);
}

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
	return date("Y-m-d",$time-DS);
}

function dump_data($Table,$arr){
	global $db;
	$str = "";
	foreach($arr as $v){
		if($v[3]){
			$str .= "(".$v[0].",".$v[1].",".$v[2].",".$v[3].",".$v[4]."),";
		}else{
			$str .= "(".$v[0].",".$v[1].",".$v[2].",null,null),";
		}
	}
	$str = trim($str,",");
	$sql = "insert into ".$Table." (`ltime`,`x`,`y`,`count_time`,`dist`) values ".$str.";";
	$db->query($sql);
}

function log_dist($startTime,$totalDist){
	global $db;
	$sql = "insert into history_dist (`time`,`dist`) values ({$startTime},{$totalDist});";
	$db->query($sql);
}

function log_log_date($startTime){
	global $db;
	$sd = date("y-m-d",$startTime);
	$sql = "UPDATE `car_map`.`last_log_date` SET `date` = '{$sd}'";
	$db->query($sql);
}

function check_table($Table){
	global $db;
	$sql = "create table IF NOT EXISTS {$Table} "."(
		`ltime` int( 11 ) NOT NULL ,
		`x` double( 21, 10 ) NOT NULL ,
		`y` double( 21, 10 ) NOT NULL ,
		`count_time` int( 11 ) default NULL ,
		`dist` float( 11, 5 ) default NULL ,
		KEY `ltime` ( `ltime` )
		) ENGINE = MYISAM DEFAULT CHARSET = utf8;";
	$db->query($sql);
}
//----------------------------------------------------------------------------------

?>
