<tr>
<td>``controllerId``</td>
<td>optional, string</td>
<td>Name of the controller to re-deploy to.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"controllerId": "testsuite"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``folder``</td>
<td>optional, string</td>
<td>Path of a folder of multiple objects to re-deploy.</td>
<td>
  <div>{</div>
  <div style="padding-left:10px;">"folder": "/myWorkflows/ifElseWorkflows"</div>
  <div>}</div>
</td>
<td></td>
</tr>
<tr>
<td>``excludes``</td>
<td>optional, array</td>
<td>Set of objects to exclude from the re-deployment.</td>
<td>
  <div>"excludes" : [ {</div>
  <div style="padding-left:20px;">"path": "/myWorkflows/ifElseWorkflows/workflow_01",</div>
  <div style="padding-left:20px;">"invConfigurationId": "28"</div>
  <div style="padding-left:10px;">}, {</div>
  <div style="padding-left:10px;">"path" : "/myWorkflows/myIfElseWorkflows/workflow_02",</div>
  <div style="padding-left:20px;">"invConfigurationId" : 75</div>
  <div style="padding-left:10px;">}</div>
  <div>]</div>
</td>
<td></td>
</tr>
