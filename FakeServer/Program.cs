using System;
using System.Linq;
using System.Text;
using System.Net;
using System.Web;
using System.Xml;
using System.Xml.Linq;
using System.Xml.Serialization;
using System.IO;

namespace FakeServer {



    class Program {



        static void Main(string[] args) {
            Random random = new Random();

            HttpListener listener = new HttpListener();
            listener.Prefixes.Add("http://*:8888/");
            Console.WriteLine("HaTechnion fake server");
            listener.Start();
            Console.WriteLine("start listening!");


            while(true) {
                HttpListenerContext context = listener.GetContext();
                HttpListenerRequest request = context.Request;
                Console.WriteLine(request.HttpMethod + ":" + request.Url.LocalPath);

                HttpListenerResponse response = context.Response;
                string responseString = "<HTML><BODY> What do you want?</BODY></HTML>";
                if(request.HttpMethod == "POST" && request.Url.LocalPath == "/fake") {
                    try {
                        string requestDetail = "";
                        using(var reader = new StreamReader(request.InputStream)) {
                            requestDetail = reader.ReadToEnd();
                        }
                        const string requestHeader = "appRequest=";
                        if(requestDetail.IndexOf(requestHeader) != 0)
                            throw new Exception("invalid request");
                        requestDetail = HttpUtility.UrlDecode(requestDetail.Substring(requestHeader.Length));

                        XmlDocument xmlIn = new XmlDocument();
                        xmlIn.LoadXml(requestDetail);
                        XmlNode nodeApp = xmlIn.SelectSingleNode("app");
                        string req = nodeApp.SelectSingleNode("req").InnerText;
                        string club = nodeApp.SelectSingleNode("club").InnerText;
                        Console.WriteLine("req=" + req);
                        if(club != "81") throw new Exception("club!=81");

                        string retCode = "0";
                        string retText = "";
                        XElement dataX = new XElement("data");
                        switch(req) {
                        case "203": {
                                string username = nodeApp.SelectSingleNode("login_name").InnerText;
                                string password = nodeApp.SelectSingleNode("pass").InnerText;
                                string unique = nodeApp.SelectSingleNode("unique_id").InnerText;
                                var cursor = from s in Data.students where s.username == username select s;
                                if(cursor.Any()) {
                                    Student student = cursor.First();
                                    if(student.password == password) {
                                        student.unique = unique;
                                        student.ticket = random.Next().ToString();
                                        dataX = new XElement("data",
                                            new XElement("id", student.ticket),
                                            new XElement("name", student.real),
                                            new XElement("user_id", username));
                                    } else {
                                        retCode = "-1";
                                        retText = "wrong password";
                                    }
                                } else {
                                    retCode = "-1";
                                    retText = "bad login name";
                                }
                                break;
                            }
                        case "182":
                        case "185": {
                                string ticket = nodeApp.SelectSingleNode("id").InnerText;
                                string unique = nodeApp.SelectSingleNode("unique_id").InnerText;
                                var cursor = from s in Data.students where
                                             s.unique == unique && s.ticket == ticket select s;
                                if(cursor.Any()) {
                                    Student student = cursor.First();
                                    XmlSerializer ser;
                                    using(var stream = new MemoryStream()) {
                                        if(req == "185") {
                                            ser = new XmlSerializer(typeof(YearGrades));
                                            ser.Serialize(stream, student.yearGrades);
                                        } else if(req == "182") {
                                            ser = new XmlSerializer(typeof(Exams));
                                            ser.Serialize(stream, student.exams);
                                        }

                                        stream.Position = 0;
                                        using(XmlReader reader = XmlReader.Create(stream)) {
                                            dataX = XElement.Load(reader);
                                            dataX.Name = "data";
                                            dataX.RemoveAttributes();
                                        }
                                    }
                                } else {
                                    retCode = "-1";
                                    retText = "bad ticket";
                                }
                                break;
                            }
                        default:
                            throw new Exception("unknown req");
                        }

                        responseString = new XElement("app",
                            new XElement("result",
                                new XElement("ret_code", retCode),
                                new XElement("text", retText)),
                            dataX).ToString();

                        Console.WriteLine(responseString);

                    } catch(Exception e) {
                        Console.WriteLine(e);
                    }
                }
                byte[] buffer = Encoding.UTF8.GetBytes(responseString);
                response.ContentLength64 = buffer.Length;
                var output = response.OutputStream;
                output.Write(buffer, 0, buffer.Length);
                output.Close();

            }



        }
    }
}
