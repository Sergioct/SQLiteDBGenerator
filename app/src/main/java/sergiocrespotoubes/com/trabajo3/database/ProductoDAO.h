#import <Foundation/Foundation.h>
#import <sqlite3.h>
#import "Producto.h"

@interface ProductoDAO : NSObject{
	sqlite3 *db;
}

+ (ProductoDAO *) instance;

- (void) createObject:(Producto*) item;

- (NSMutableArray *) getAll;

- (Producto *) getById:(NSInteger)auxid;

- (void) updateObject:(Producto*) item;

- (void) deleteObject:(NSInteger)auxid;

@end