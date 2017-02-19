#import "ProductoDAO.h"
#import "Producto.h"

@implementation ProductoDAO

static Producto *instance;

+ (Producto *) instance {
	if(instance == nil){
		instance = [[Producto alloc] init];
	}
	return instance;
}

- (NSMutableArray *) getAll{

	const char *sql =  "SELECT * FROM Producto";

	NSMutableArray * list = [[NSMutableArray alloc] init];

	Producto *item = [[Producto alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.nombre = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 0)];
			item.coste = sqlite3_column_int(sqlStatement, 1);
		[list addObject:item];
		}
		sqlite3_finalize(sqlStatement);
		sqlite3_close(db);
	}
	return list;
}

- (Producto *) getById:(NSInteger)auxid{

	const char *sql =  [[NSString stringWithFormat:@"SELECT * FROM Producto where id=%ld",auxid] UTF8String];

	Producto *item = [[Producto alloc] init];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK)
	{
		if(sqlite3_step(sqlStatement) == SQLITE_ROW){
			item.nombre = [NSString stringWithUTF8String:(char *)sqlite3_column_text(sqlStatement, 0)];
			item.coste = sqlite3_column_int(sqlStatement, 1);
		}
		sqlite3_finalize(sqlStatement);
		sqlite3_close(db);
	}
	return item;
}

-(void)createObject:(Producto *)item {
	NSString *sqlInsert = [NSString stringWithFormat:@"Insert into Producto(NOMBRE, COSTE) VALUES ('%@', '%@')", item.nombre, item.coste];


	const char *sql = [sqlInsert UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

- (void) updateObject:(Producto*)item{


	const char *sql = [[NSString stringWithFormat:@"update Producto nombre = %@, coste = %ld where id=%ld" , item.nombre, item.coste, item.myid] UTF8String];

	sqlite3_stmt *sqlStatement;

	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}

}

- (void) deleteObject:(NSInteger)auxid{

	const char *sql = [[NSString stringWithFormat:@"delete from Producto where myid=%ld",auxid] UTF8String];

	sqlite3_stmt *sqlStatement;
	if(sqlite3_prepare_v2(db, sql, 2, &sqlStatement, NULL) == SQLITE_OK){
		sqlite3_step(sqlStatement);
		sqlite3_finalize(sqlStatement);
	}
}

@end