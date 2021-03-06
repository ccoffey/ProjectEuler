from __future__ import with_statement
import urllib, urllib2, cookielib
import sqlite3
from BeautifulSoup import BeautifulSoup
from urllib import urlretrieve
import shutil
from contextlib import closing
import os

cookie_jar = cookielib.CookieJar()
opener = urllib2.build_opener(urllib2.HTTPCookieProcessor(cookie_jar))
urllib2.install_opener(opener)

params = urllib.urlencode({'username': 'cathal', 'password': 'pi=3.1415', 'login': 'Login'})
url = 'http://projecteuler.net/login'

req = urllib2.Request(url, params)
response = urllib2.urlopen(req)
the_page = response.read()

req = urllib2.Request('http://projecteuler.net/minimal=problems')
response = urllib2.urlopen(req)
the_page = response.read()

if os.path.exists('../assets/tmp'):
    os.remove('../assets/tmp')

if os.path.exists('../assets/assets.zip'):
    os.remove('../assets/assets.zip')

os.makedirs('../assets/tmp/databases')
con = sqlite3.connect('../assets/tmp/databases/euler.db')
con.text_factory = str
con.execute('''create table data(_id integer primary key, title text, published integer, updated integer, solvedby integer, solved integer, html text, answer text)''')
con = sqlite3.connect('../assets/tmp/databases/euler.db')
c = con.cursor()

count = 0
for line in the_page.split('\n'):
    if not line == '':
        count += 1
        line = line.decode("utf-8")
        (_id, title, published, updated, solvedby, solved, answer) = line.split('##')
        
        print 'Problem %s' % _id
        
        req = urllib2.Request('http://projecteuler.net/minimal=%s' % _id)
        response = urllib2.urlopen(req)
        html = response.read()
        soup = BeautifulSoup(html)
        for img in soup.findAll('img'):
            if img['src'].startswith('http://projecteuler.net/'):
                img['src'] = img['src'][len('http://projecteuler.net/'):]
            src = img['src']
            if not os.path.exists('../assets/tmp/' + src):
                print '\t%s' % src
                if not os.path.exists('../assets/tmp/' + src[0:src.rindex('/')]):
                    os.makedirs('../assets/tmp/' + src[0:src.rindex('/')])
                urlretrieve('http://projecteuler.net/%s' % src, '../assets/tmp/' + src)
        html = soup.prettify().decode("utf-8")
        c.execute('insert into data values (?,?,?,?,?,?,?,?)', (int(_id), title, int(published), int(updated), int(solvedby), 0, html, answer))

con.commit()
shutil.copy('../res/drawable-xhdpi/login.png', '../assets/tmp/login.png')
shutil.copy('../res/drawable-xhdpi/login_old.png', '../assets/tmp/login_old.png')

os.chdir('../assets/tmp/')
os.system('zip -r ../assets.zip .')

if os.path.exists('../tmp'):
    shutil.rmtree('../tmp')

