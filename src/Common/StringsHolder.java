package Common;


/**
* Common/StringsHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from FileServer.idl
* Saturday, August 24, 2019 10:21:54 o'clock PM EDT
*/

public final class StringsHolder implements org.omg.CORBA.portable.Streamable
{
  public String value[] = null;

  public StringsHolder ()
  {
  }

  public StringsHolder (String[] initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = Common.StringsHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    Common.StringsHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return Common.StringsHelper.type ();
  }

}