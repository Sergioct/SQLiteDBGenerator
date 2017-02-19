#import "SegundaDAO.h"
#import "Segunda.h"

@implementation SegundaDAO

static Segunda *instance;

+ (Segunda *) instance {
	if(instance == nil){
		instance = [[Segunda alloc] init];
	}
	return instance;
}

- (NSMutableArray *) getAll{

	const char *sql =  "SELECT * FROM Segunda";

	NSMutableArray * list = [[NSMutableArray alloc] init];

	Segunda *item = [[Segunda alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.col_fecha = sqlite3_column_int(sqlStatement, 0);
		[list addObject:item];
		}
		sqlite3_finalize(sqlStatement);
		sqlite3_close(db);
	}
	return list;
}

- (Segunda *) getById:(NSInteger)auxid{

	const char *sql =  [[NSString stringWithFormat:@"SELECT * FROM Segunda where id=%ld",auxid] UTF8String];

	Segunda *item = [[Segunda alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.col_fecha = sqlite3_column_int(sqlStatement, 0);
		}
		sqlite3_finalize(sqlStatement);
		sqlite3_close(db);
	}
	return item;
}

-(void)createObject:(Segunda *)item {
	NSString *sqlInsert = [NSString stringWithFormat:@"Insert into Segunda(COL_FECHA) VALUES ('%ld')", (long)item.col_fecha];


	const char *sql = [sqlInsert UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

- (void) updateObject:(Segunda*)item{


	const char *sql = [[NSString stringWithFormat:@"update Segunda col_fecha = %ld where id=%ld" , item.col_fecha, item.myid] UTF8String];

	sqlite3_stmt *sqlStatement;

	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}

}

- (void) deleteObject:(NSInteger)auxid{

	const char *sql = [[NSString stringWithFormat:@"delete from Segunda where myid=%ld",auxid] UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

@end