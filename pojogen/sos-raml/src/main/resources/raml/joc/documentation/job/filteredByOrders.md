<tr><td>``orders``</td><td>optional, array</td>
<td>Filtered response by a collection of orders specified by its workflow path and an optional order id and position.<br/>
If "orderId" and "position" is undefined then all orders of the specified workflow are included in the response.<br/>
If "orderId" is specified then parameters such as ``folders``, ``types``, ``excludeJobs`` and ``regex`` are ignored.</td>
<td> [{
  <div style="padding-left:10px;">"workflow":"/sos/reporting/Inventory",</div>
  <div style="padding-left:10px;">"orderId":"Inventory"</div>
  <div style="padding-left:10px;">"position":"exec"</div>
  }]</td>
<td></td>
</tr>
