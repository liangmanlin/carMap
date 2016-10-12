<?php

 class DB{
    private $con=null;
    private $query_result = null;
    private $rows;
    public function DB(){
        $this->con = mysql_connect("localhost","root","Tom#Jerry&Z");
        if (!mysql_select_db("car_map", $this->con)) {
            throw new Exception("1" . mysql_error());		
        }
    }
    
    public function query($sql)
	{
		$sql = trim($sql);
		if ($sql == '') {
			throw new Exception("SQL2");
		}
		
		$result = mysql_query($sql, $this->con);
		if ($result === false) {
			throw new Exception("sql3" . $sql . "   " . mysql_error());
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
		throw new Exception("4");
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
		throw new Exception("5");
	}
 }
?>