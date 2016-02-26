using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FakeServer {
    public class grade {
        public string name;
        public string final_grade;
        public string credits;
    }
    public class year_grade {
        public string year;
        public string year_system;
        public string average_grade;
        public grade[] grades;
    }

    public class YearGrades {
        public year_grade[] year_grades;
    }


    public class Student {
        public string username;
        public string password;
        public string real;
        public string unique;
        public string ticket;
        public YearGrades yearGrades;

    }

    public class Data {
        public static Student[] students = new Student[] {
            new Student {
                username="wwylele",password="pass",real="WWYLELE",
                yearGrades=new YearGrades { year_grades=new year_grade[] {
                    new year_grade {
                        year="first",year_system="1",average_grade="50",
                        grades=new grade[] {
                            new grade {
                                name="foo",final_grade="90",credits="3"
                            },
                            new grade {
                                name="bar",final_grade="10",credits="2"
                            }
                        }
                    },
                    new year_grade {
                        year="second",year_system="2",average_grade="100",
                        grades=new grade[] {
                            new grade {
                                name="f***",final_grade="100",credits="0"
                            }
                        }
                    },
                } }
            },
            new Student {
                username="test",password="123456",real="TEST!",
                yearGrades=new YearGrades { year_grades=new year_grade[] {
                    new year_grade {
                        year="test year",year_system="-1",average_grade="0",
                        grades=new grade[] {
                            new grade {
                                name="what",final_grade="0",credits="10"
                            }
                        }
                    },
                } }
            },
        };
    }
}
