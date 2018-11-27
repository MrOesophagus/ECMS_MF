/*
	Change Management
	Written by: kcsmbf 13/11/18 (code structure from kiseeg)
	Change Log:
*/

if (_encode) {
  _result = _input.bytes.encodeBase64().toString()
} else {
  byte[] decoded = _input.decodeBase64()
  _result = new String(decoded)
}