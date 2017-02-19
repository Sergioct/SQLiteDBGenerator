#import "ProductoDAO.h"
#import "Producto.h"

@implementation ProductoDAO

static ProductoDAO *instance;

+ (ProductoDAO *) instance {
	if(instance == nil){
		instance = [[ProductoDAO alloc] init];
	}
	return instance;
}

- (id)init {
	if ((self = [super init])) {
		NSArray* paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		NSString* documentsDirectory = [paths lastObject];
		NSString* databasePath = [documentsDirectory stringByAppendingPathComponent:@"database.sqlite3"];
		if (sqlite3_open([sqLiteDb UTF8String], &db) != SQLITE_OK) {
			NSLog(@"Failed to open database!");
		}
	}
	return self;
}

- (NSMutableArray *) getAll{

	const char *sql =  "SELECT * FROM Producto";

	NSMutableArray * list = [[NSMutableArray alloc] init];

	Producto *item = [[Producto alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.myid = sqlite3_column_int(sqlStatement, 0);
			item.nombre = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 1)];
			item.coste = sqlite3_column_int(sqlStatement, 2);
		[list addObject:item];
		}
		sqlite3_finalize(sqlStatement);
	}
	return list;
}

- (Producto *) getById:(NSInteger)auxid{

	const char *sql =  [[NSString stringWithFormat:@"SELECT * FROM Producto where id=%ld",auxid] UTF8String];

	Producto *item = [[Producto alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.myid = sqlite3_column_int(sqlStatement, 0);
			item.nombre = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 1)];
			item.coste = sqlite3_column_int(sqlStatement, 2);
		}
		sqlite3_finalize(sqlStatement);
	}
	return item;
}

-(void)createObject:(Producto *)item {
	NSString *sqlInsert = [NSString stringWithFormat:@"Insert into Producto(NOMBRE, COSTE) VALUES ('%@', '%@')", item.nombre, item.coste];


	const char *sql = [sqlInsert UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

- (void) updateObject:(Producto*)item{


	const char *sql = [[NSString stringWithFormat:@"update Producto set nombre = '%@', coste = '%ld' where id=%ld" , item.nombre, item.coste, item.myid] UTF8String];

	sqlite3_stmt *sqlStatement;

	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}

}

- (void) deleteObject:(NSInteger)auxid{

	const char *sql = [[NSString stringWithFormat:@"delete from Producto where myid=%ld",auxid] UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, -1, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

@end