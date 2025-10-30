package util

import java.util.ArrayList
import com.onresolve.scriptrunner.db.DatabaseUtil
def query = """
select c.pageid "pageID", b.body "risID" from bodycontent b
join content c on b.contentid = c.contentid
where b.contentid in
(select contentid from content
where content.title = 'callID')
"""
def list = new ArrayList()
def sql_rows = DatabaseUtil.withSql('local'){ sql ->
    sql.rows(query)
}

return list
