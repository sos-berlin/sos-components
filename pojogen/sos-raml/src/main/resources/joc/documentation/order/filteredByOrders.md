<tr><td>``orders``</td><td>optional, array</td>
<td>Filtered response by a collection of orders specified by its workflow path and an optional order id.<br/>
If "orderId" is undefined then all orders of the specified workflow are included in the response.<br/>
If "orderId" is specified then parameters such as ``folders``, ``types``, ``excludeOrders`` and ``regex`` are ignored.</td>
<td> [{
  <div style="padding-left:10px;">"workflow":"/path/to/workflow",</div>
  <div style="padding-left:10px;">"orderId":"myOrder"</div>
  }]</td>
<td></td>
</tr>
