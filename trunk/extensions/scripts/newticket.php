<?php

function do_call($host, $port, $request) {
  
   $fp = fsockopen($host, $port, $errno, $errstr);
   $query = "POST /home/servertest.php HTTP/1.0\nUser_Agent: My Egg Client\nHost: ".$host."\nContent-Type: text/xml\nContent-Length: ".strlen($request)."\n\n".$request."\n";

   if (!fputs($fp, $query, strlen($query))) {
       $errstr = "Write error";
       return 0;
   }

   $contents = '';
   while (!feof($fp)) {
       $contents .= fgets($fp);
   }

   fclose($fp);

   # strip header
   $xml=(substr($contents, strpos($contents, "\r\n\r\n")+4));

   # convert XML to PHP values
   return xmlrpc_decode($xml);
}


$host = "localhost";
$port = 12345;

$moduleName = "PAC";
$issueType = "Defect";
$userName = "jon@latchkey.com";

$attribs = array('Status' => 'New', 'Description' => 'Some interesting stuff from python', 'AssignedTo' => $userName);

$req = xmlrpc_encode_request('newTicket.createNewTicket', array($moduleName, $issueType, $userName, $attribs));
echo $req, "\n";

$resp = do_call($host, $port, $req);

echo $resp, "\n";


?> 
