dialogue-engine
===============

Engine for making conversation in games. Supports option for subdialogues, and changing topics.


##XML format

Each character has a `tree` tag as its root, and needs an identifier. The main elements of the `tree` are `dialog` tags.
They each have `id`s, which can be any string, however, the entry point for the conversation must have the id "root".
Each `dialog` has a `message`, and several `answer`s under it. `answer` nodes have two attributes, `goto` and `type`.
`goto` specifies an id of another `dialog` node or `subdialog` node. The use of `subdialog` nodes in `dialog` nodes allows
for the organization of the conversation into topics. The `goto` attribute also supports the value "quit", which will
exit the conversation. `type` specifies what kind of `goto` you are using. Use "next" to go to a `dialog` and "sub" to go to
a `subdialog`.

###Sample

```xml
<tree character="example">

  <dialog id="root">
  
    <message>This is an example</message>
      <answers>
        <answer goto="agreement" type="next">Yes, it is!</answer>
        <answer goto="explain" type="sub">Please, tell me more.</answer>
      </answers>
  
    <subdialog id="explain">
      <message>That's pretty much all there is to it!</message>
        <answers>
          <answer goto="quit" type="next">Great, thanks!</answer>
        </answers>
    </subdialog>
    
  </dialog>
  
  <dialog id="agreement>
  
    <message>I'm so glad that you agree.</message>
      <answers>
        <answer goto="quit" type="next">Yes, good bye.</answer>
      </answers>
  
  </dialog>
</tree>
```
